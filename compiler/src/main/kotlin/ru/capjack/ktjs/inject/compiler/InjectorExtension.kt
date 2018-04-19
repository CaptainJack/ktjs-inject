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
			if (descriptor.kind != ClassKind.INTERFACE) {
				error(declaration, "InjectProxy can only be an interface")
			}
			else {
				
				val methods = JsArrayLiteral(
					descriptor.unsubstitutedMemberScope.getContributedDescriptors(
						DescriptorKindFilter.FUNCTIONS,
						alwaysTrue()
					)
						.filterIsInstance<SimpleFunctionDescriptor>()
						.filter { it.modality == Modality.ABSTRACT && it.returnType != null }
						.map {
							JsArrayLiteral(
								listOf(
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
		}
		else if (descriptor.annotations.hasAnnotation(annotationInject)) {
			if (descriptor.kind != ClassKind.CLASS) {
				error(declaration, "Inject can only be an class")
			}
			else if (descriptor.modality == Modality.ABSTRACT) {
				error(declaration, "Inject can only be an non abstract class")
			}
			else {
				val types = JsArrayLiteral(
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
				val create = context.createRootScopedFunction("create")
				val args = create.addParameter("a")
				create.addStatement(
					JsReturn(
						JsNew(
							context.getInnerReference(descriptor),
							0.until(types.expressions.size).map {
								JsArrayAccess(
									JsNameRef(args.name),
									JsNameRef(it.toString())
								)
							})
					)
				)
				
				context.addTopLevelStatement(
					JsAstUtils.assignment(
						JsNameRef(
							"inject",
							JsNameRef(Namer.METADATA, context.getInnerReference(descriptor))
						), JsObjectLiteral(
							listOf(
								JsPropertyInitializer(JsNameRef("types"), types),
								JsPropertyInitializer(JsNameRef("create"), create)
							),
							true
						)
					).makeStmt()
				)
			}
		}
	}
	
	private fun defineTypeReference(context: TranslationContext, type: KotlinType): JsExpression {
		return JsInvocation(
			context.getReferenceToIntrinsic(Namer.GET_KCLASS),
			getReferenceToJsClass(type, context)
		)
	}
}