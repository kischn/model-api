package codegen

/**
 * @author kischn
 * @date 2021/8/12
 */
open class ClassInfo(val packageName: String, val name: String, val genericType: String = "") {

    /**
     * 额外的引包
     */
    val extImports: MutableList<String> = ArrayList()

    /**
     * 类上面的注解
     */
    val annotations: MutableList<String> = ArrayList()

    /**
     * 继承的类
     */
    val inheritClass: String? = null

    /**
     * 实现的接口
     */
    val implementedInterface: List<String> = ArrayList()

    /**
     * 属性
     */
    val fields: MutableList<ClassField> = ArrayList()

    fun buildClassFile(): String {
        val importPart = extImports.joinToString("\r\n", postfix = ";")
        val implPart =
            if (implementedInterface.isEmpty()) "" else implementedInterface.joinToString(",", " implements ")
        val classAnnotationText = annotations.joinToString("\r\n")
        val extendPart = inheritClass?.let { "extends $it" } ?: ""
        val fieldsPart = fields.joinToString("\r\n") { field ->
            val fieldAnnotationText = field.annotations.joinToString("\r\n")
            "$fieldAnnotationText\r\n private ${field.type} ${field.name};"
        }
        //TODO 添加方法相关拼接处理
        return """
            package $packageName;
            
            $importPart
            
            $classAnnotationText
            public class $name $extendPart $implPart {
                $fieldsPart
            }
            
            
        """.trimIndent()
    }
}

data class ClassExt(
    val type: String,
    val name: String
)

/**
 * 类属性
 */
data class ClassField(
    val name: String,
    val type: String,
    val annotations: MutableList<String> = ArrayList(),
)

/**
 * 类方法
 */
data class ClassMethod(
    val name: String,
    val args: MutableList<MethodArg>,
    val annotations: MutableList<String>,
    val ret: ClassField
)

/**
 * 方法参数
 */
data class MethodArg(
    val name: String,
    val type: String,
    val annotations: MutableList<String>
)