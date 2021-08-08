package base

import openapiv3.SchemaObject
import openapiv3.SchemaObjectDef
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
}

/**
 * 字符串类型
 */
class StringField(name: String, remark: String?) : Field(name, remark) {
    var size: IntRange? = null
    var pattern: Pattern? = null
    override fun toSchemaObject(): SchemaObject {
        return SchemaObjectDef(type = "string")
    }
}

/**
 * 数值类型
 */
class IntegerField(name: String, remark: String?) : Field(name, remark) {
    var range: IntRange? = null
    override fun toSchemaObject(): SchemaObject {
        return SchemaObjectDef(type = "integer", format = "int32")
    }
}

/**
 * 日期类型
 */
class DateField(name: String, remark: String?) : Field(name, remark) {
    override fun toSchemaObject(): SchemaObject {
        return SchemaObjectDef(type = "integer", format = "int64")
    }
}

/**
 * int 列表
 */
class IntListField(name: String, remark: String?) : Field(name, remark) {
    override fun toSchemaObject(): SchemaObject {
        return SchemaObjectDef(
            type = "array",
            items = SchemaObjectDef(type = "integer", format = "int32")
        )
    }
}

/**
 * 字符串列表
 */
class StringListField(name: String, remark: String?) : Field(name, remark) {
    override fun toSchemaObject(): SchemaObject {
        return SchemaObjectDef(
            type = "array",
            items = SchemaObjectDef(type = "string")
        )
    }
}

/**
 * 自定义对象列表
 */
class ModelListField(name: String, remark: String, private val modelDef: ModelDefinition) : Field(name, remark) {
    override fun toSchemaObject(): SchemaObject {
        return SchemaObjectDef(type = "array", items = modelDef.toSchemaObject())
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
}
