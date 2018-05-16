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
import org.jetbrains.kotlin.resolve.descriptorUtil.firstArgument
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.utils.alwaysTrue

class InjectorExtension(
	private val messages: MessageCollector
) : JsSyntheticTranslateExtension {
	
	private val annotationInject = FqName("ru.capjack.ktjs.inject.Inject")
	private val annotationInjectName = FqName("ru.capjack.ktjs.inject.InjectName")
	private val annotationInjectProxy = FqName("ru.capjack.ktjs.inject.InjectProxy")
	
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
		if (descriptor.annotations.hasAnnotation(annotationInjectProxy)) {
			processInjectProxy(declaration, descriptor, context)
		}
		else if (descriptor.annotations.hasAnnotation(annotationInject)) {
			processInject(declaration, descriptor, context)
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
				val annotation = it.annotations.findAnnotation(annotationInjectName)
				val type = defineTypeReference(context, it.type)
				if (annotation == null) {
					type
				}
				else {
					JsArrayLiteral(
						listOf(
							type,
							JsStringLiteral(annotation.firstArgument()?.value?.toString() ?: it.name.identifier)
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
	
	private fun processInjectProxy(declaration: KtPureClassOrObject, descriptor: ClassDescriptor, context: TranslationContext) {
		if (descriptor.kind != ClassKind.INTERFACE) {
			error(declaration, "InjectProxy can only be an interface")
			return
		}
		
		val methods = JsArrayLiteral(
			descriptor.unsubstitutedMemberScope.getContributedDescriptors(DescriptorKindFilter.FUNCTIONS, alwaysTrue())
				.filterIsInstance<SimpleFunctionDescriptor>()
				.filter { it.modality == Modality.ABSTRACT && it.returnType != null }
				.map {
					JsArrayLiteral(
						listOf(
							JsStringLiteral(it.name.identifier),
							JsStringLiteral(context.getNameForDescriptor(it).ident),
							defineTypeReference(context, it.returnType!!),
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
	
	private fun defineTypeReference(context: TranslationContext, type: KotlinType): JsExpression {
		return JsInvocation(
			context.getReferenceToIntrinsic(Namer.GET_KCLASS),
			getReferenceToJsClass(type, context)
		)
	}
}