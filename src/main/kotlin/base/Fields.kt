package base

import openapiv3.SchemaObject
import java.util.regex.Pattern

/**
 * @author kischn
 */
abstract class Field(
    /**
     * 名称
     */
    val name: String,
    /**
     * 备注
     */
    val remark: String?
) {
    var nullable: Boolean = true

    /**
     * 转成 schemaObject
     */
    abstract fun toSchemaObject(): SchemaObject

    open fun show() {
        println(toString())
    }
}

/**
 * 字符串类型
 */
class StringField(name: String, remark: String?) : Field(name, remark) {
    var size: IntRange? = null
    var pattern: Pattern? = null
    override fun toSchemaObject(): SchemaObject {
        return SchemaObject(type = "string")
    }

    override fun toString(): String {
        return "string(name=$name,remark=$remark,size=$size,pattern=$pattern)"
    }
}

/**
 * 数值类型
 */
class IntegerField(name: String, remark: String?) : Field(name, remark) {
    var range: IntRange? = null
    override fun toSchemaObject(): SchemaObject {
        return SchemaObject(type = "integer", format = "int32")
    }

    override fun toString(): String {
        return "int(name=$name,remark=$remark,range=$range)"
    }
}

/**
 * 日期类型
 */
class DateField(name: String, remark: String?) : Field(name, remark) {
    override fun toSchemaObject(): SchemaObject {
        return SchemaObject(type = "integer", format = "int64")
    }

    override fun toString(): String {
        return "date(name=$name,remark=$remark)"
    }
}

/**
 * int 列表
 */
class IntListField(name: String, remark: String?) : Field(name, remark) {
    override fun toSchemaObject(): SchemaObject {
        return SchemaObject(
            type = "array",
            items = SchemaObject(
                type = "int",
                format = "int32"
            )
        )
    }

    override fun toString(): String {
        return "list<int>(name=$name,remark=$remark)"
    }
}

/**
 * 字符串列表
 */
class StringListField(name: String, remark: String?) : Field(name, remark) {
    override fun toSchemaObject(): SchemaObject {
        return SchemaObject(
            type = "array",
            items = SchemaObject(
                type = "string"
            )
        )
    }

    override fun toString(): String {
        return "list<string>(name=$name,remark=$remark)"
    }
}

/**
 * 自定义对象列表
 */
class ModelListField(name: String, remark: String, private val modelDef: ModelDefinition) : Field(name, remark) {
    override fun toSchemaObject(): SchemaObject {
        return SchemaObject(
            type = "array",
            items = modelDef.toSchemaObject()
        )
    }

    override fun toString(): String {
        return "list<Object>(name=$name,remark=$remark)"
    }
}

/**
 * 自定义对象
 */
class ModelDefinitionField(
    name: String,
    remark: String?,
    private val modelDef: ModelDefinition
) : Field(name, remark) {
    override fun toSchemaObject(): SchemaObject {
        return modelDef.toSchemaObject()
    }

    override fun show() {
        println("field,name:$name,subModule:")
        modelDef.print()
    }

    override fun toString(): String {
        return "$name>$modelDef"
    }
}

