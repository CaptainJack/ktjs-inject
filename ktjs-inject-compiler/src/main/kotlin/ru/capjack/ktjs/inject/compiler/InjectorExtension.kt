package ru.capjack.ktjs.inject.compiler

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticUtils
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.declaration.DeclarationBodyVisitor
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.getReferenceToJsClass
import org.jetbrains.kotlin.js.translate.utils.jsAstUtils.addParameter
import org.jetbrains.kotlin.js.translate.utils.jsAstUtils.addStatement
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtPureClassOrObject
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.BooleanValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.firstArgument
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.utils.alwaysTrue

class InjectorExtension(
	private val messages: MessageCollector
) : JsSyntheticTranslateExtension {
	
	private val annotationInject = FqName("ru.capjack.ktjs.inject.Inject")
	private val annotationProxy = FqName("ru.capjack.ktjs.inject.Proxy")
	private val annotationBind = FqName("ru.capjack.ktjs.inject.Bind")
	private val annotationBindSelf = FqName("ru.capjack.ktjs.inject.BindSelf")
	private val annotationBindProxy = FqName("ru.capjack.ktjs.inject.BindProxy")
	private val annotationName = FqName("ru.capjack.ktjs.inject.Name")
	private val annotationProvides = FqName("ru.capjack.ktjs.inject.Provides")
	
	private fun error(declaration: KtPureClassOrObject, message: String) {
		val psi = declaration.psiOrParent
		val lineAndColumn = DiagnosticUtils.getLineAndColumnInPsiFile(psi.containingFile, psi.textRange)
		messages.report(
			CompilerMessageSeverity.ERROR,
			message,
			CompilerMessageLocation.create(
				psi.containingFile.virtualFile.path,
				lineAndColumn.line, lineAndColumn.column, lineAndColumn.lineContent
			)
		)
	}
	
	override fun generateClassSyntheticParts(declaration: KtPureClassOrObject, descriptor: ClassDescriptor, translator: DeclarationBodyVisitor, context: TranslationContext) {
		if (descriptor.annotations.hasAnnotation(annotationInject)) {
			processInject(declaration, descriptor, context)
		}
		
		if (descriptor.annotations.hasAnnotation(annotationProxy)) {
			processProxy(declaration, descriptor, context)
		}
		
		if (descriptor.annotations.hasAnnotation(annotationBind)) {
			processBind(descriptor, context)
		}
		
		if (descriptor.annotations.hasAnnotation(annotationBindSelf)) {
			processBindSelf(descriptor, context)
		}
		
		if (descriptor.annotations.hasAnnotation(annotationBindProxy)) {
			processBindProxy(descriptor, context)
		}
	}
	
	private fun processInject(declaration: KtPureClassOrObject, descriptor: ClassDescriptor, context: TranslationContext) {
		if (descriptor.kind != ClassKind.CLASS) {
			error(declaration, "Inject can only be an class")
			return
		}
		
		if (descriptor.modality == Modality.ABSTRACT) {
			error(declaration, "Inject can only be an non abstract class")
			return
		}
		
		val args = JsArrayLiteral(
			descriptor.constructors.first { it.isPrimary }.valueParameters.map {
				val ann = it.annotations.findAnnotation(annotationName)
				val type = defineTypeReference(context, it.type)
				if (ann == null) {
					type
				} else {
					JsArrayLiteral(
						listOf(
							type,
							JsStringLiteral(ann.firstArgument()?.value?.toString() ?: it.name.identifier)
						)
					)
				}
			}
		)
		val create = context.createRootScopedFunction("create").apply {
			val pArgs = addParameter("a")
			addStatement(
				JsReturn(
					JsNew(
						context.getInnerReference(descriptor),
						0.until(args.expressions.size).map {
							JsArrayAccess(JsNameRef(pArgs.name), JsIntLiteral(it))
						})
				)
			)
		}
		
		context.addTopLevelStatement(
			JsAstUtils.assignment(
				JsNameRef(
					"inject",
					JsNameRef(Namer.METADATA, context.getInnerReference(descriptor))
				), JsObjectLiteral(
					listOf(
						JsPropertyInitializer(JsNameRef("args"), args),
						JsPropertyInitializer(JsNameRef("create"), create)
					),
					true
				)
			).makeStmt()
		)
	}
	
	private fun processProxy(declaration: KtPureClassOrObject, descriptor: ClassDescriptor, context: TranslationContext) {
		if (descriptor.kind != ClassKind.INTERFACE) {
			error(declaration, "InjectProxy can only be an interface")
			return
		}
		
		val methods = JsArrayLiteral(
			descriptor.unsubstitutedMemberScope.getContributedDescriptors(DescriptorKindFilter.FUNCTIONS, alwaysTrue())
				.filterIsInstance<SimpleFunctionDescriptor>()
				.filter { it.modality == Modality.ABSTRACT && it.returnType != null }
				.map {
					val ann = it.annotations.findAnnotation(annotationProvides)
					JsArrayLiteral(
						listOf(
							JsStringLiteral(it.name.identifier),
							JsStringLiteral(context.getNameForDescriptor(it).ident),
							defineTypeReference(
								context,
								if (ann != null) (ann.argumentValue("implementation") as KClassValue).value else it.returnType!!
							),
							JsIntLiteral(it.valueParameters.size)
						)
					)
				}
		)
		
		context.addTopLevelStatement(
			JsAstUtils.assignment(
				JsNameRef(
					"injectProxy",
					JsNameRef(Namer.METADATA, context.getInnerReference(descriptor))
				),
				methods
			).makeStmt()
		)
	}
	
	private fun processBind(descriptor: ClassDescriptor, context: TranslationContext) {
		val ann = descriptor.annotations.findAnnotation(annotationBind)!!
		
		@Suppress("CAST_NEVER_SUCCEEDS")
		val argImplementation = ann.argumentValue("implementation") as KClassValue
		
		@Suppress("CAST_NEVER_SUCCEEDS")
		val argMultiple = ann.argumentValue("multiple").let {
			if (it == null) false else (it as BooleanValue).value
		}
		
		context.addTopLevelStatement(
			JsAstUtils.assignment(
				JsNameRef(
					"injectBind",
					JsNameRef(Namer.METADATA, context.getInnerReference(descriptor))
				), JsObjectLiteral(
					listOf(
						JsPropertyInitializer(JsNameRef("implementation"), defineTypeReference(context, argImplementation.value)),
						JsPropertyInitializer(JsNameRef("multiple"), JsBooleanLiteral(argMultiple))
					),
					true
				)
			).makeStmt()
		)
	}
	
	private fun processBindSelf(descriptor: ClassDescriptor, context: TranslationContext) {
		val ann = descriptor.annotations.findAnnotation(annotationBindSelf)!!
		
		@Suppress("CAST_NEVER_SUCCEEDS")
		val argMultiple = ann.argumentValue("multiple").let {
			if (it == null) false else (it as BooleanValue).value
		}
		
		context.addTopLevelStatement(
			JsAstUtils.assignment(
				JsNameRef(
					"injectBind",
					JsNameRef(Namer.METADATA, context.getInnerReference(descriptor))
				), JsObjectLiteral(
					listOf(
						JsPropertyInitializer(JsNameRef("implementation"), JsStringLiteral("self")),
						JsPropertyInitializer(JsNameRef("multiple"), JsBooleanLiteral(argMultiple))
					),
					true
				)
			).makeStmt()
		)
	}
	
	private fun processBindProxy(descriptor: ClassDescriptor, context: TranslationContext) {
		
		context.addTopLevelStatement(
			JsAstUtils.assignment(
				JsNameRef(
					"injectBind",
					JsNameRef(Namer.METADATA, context.getInnerReference(descriptor))
				), JsObjectLiteral(
					listOf(
						JsPropertyInitializer(JsNameRef("implementation"), JsStringLiteral("proxy"))
					),
					true
				)
			).makeStmt()
		)
	}
	
	private fun defineTypeReference(context: TranslationContext, type: KotlinType): JsExpression {
		return JsInvocation(
			context.getReferenceToIntrinsic(Namer.GET_KCLASS),
			getReferenceToJsClass(type, context)
		)
	}
}