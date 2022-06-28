package com.rrain.annotations

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStream


private const val annotationName = "AllArgsConstructor"


private fun KSType.isNullable() = this.nullability==Nullability.NULLABLE

private fun KSTypeReference.getString(): String {
    val typeRef = this
    val type = this.resolve()
    var typeString = type.declaration.qualifiedName!!.asString()

    this.element!!.typeArguments.let {
        if (it.isNotEmpty()) {
            typeString += "<${it.map { it.type!!.getString() }.joinToString(", ")}>"
        }
    }
    if (type.isNullable()) typeString+="?"

    return typeString
}


class AllArgsConstructorProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
) : SymbolProcessor {

    lateinit var file: OutputStream

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("com.rrain.annotations.$annotationName")
        val ret = symbols.filter { !it.validate() }.toList()
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(AllArgsConstructorVisitor(), Unit) }
        return ret
    }


    inner class AllArgsConstructorVisitor : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val hasZeroArgsCtor = classDeclaration.getConstructors()
                .any { it.isPublic() && it.parameters.all { it.hasDefault } }
            if (!hasZeroArgsCtor) return

            // todo process varargs
            val props =
                classDeclaration.primaryConstructor!!.parameters
                    .filter {
                        it.isVar
                    }
                    .map {
                        val name = it.name!!.asString()
                        val typeName = it.type.getString()
                        name to typeName
                    } +
                classDeclaration.getAllProperties()
                    .filter {
                        it.isMutable
                        && it.hasBackingField
                        && it.setter?.modifiers?.contains(Modifier.PUBLIC) ?: false
                    }
                    .map {
                        val name = it.simpleName.asString()
                        val typeName = it.type.getString()
                        name to typeName
                    }

            val packageName = classDeclaration.containingFile!!.packageName.asString()
            val className = classDeclaration.simpleName.asString()
            val file = codeGenerator.createNewFile(Dependencies(true, classDeclaration.containingFile!!), packageName, className+annotationName)
            val writer = file.bufferedWriter()

            writer.write(
"""
package $packageName

fun $className(
    ${props.map {
        "${it.first}: ${it.second}${if (it.second.endsWith("?")) " = null" else ""}"
    }.joinToString(", ")}
): $className {
    val obj = $className()
${props.map { 
"""
    obj.${it.first} = ${it.first}
""".removeSurrounding("\n") }.joinToString("\n")}
    return obj
}
""".removeSurrounding("\n"))

            writer.close()

            //classDeclaration.primaryConstructor!!.accept(this, data)
        }

        /*override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            val parent = function.parentDeclaration as KSClassDeclaration
            val packageName = parent.containingFile!!.packageName.asString()
            val className = parent.simpleName.asString()
            val file = codeGenerator.createNewFile(Dependencies(true, function.containingFile!!), packageName, className+annotationName)
        }*/

    }



}

class AllArgsConstructorProvider : SymbolProcessorProvider {
    override fun create(env: SymbolProcessorEnvironment): SymbolProcessor {
        return AllArgsConstructorProcessor(env.codeGenerator, env.logger)
    }
}