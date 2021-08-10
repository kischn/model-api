package base

import openapiv3.OpenAPIV3
import openapiv3.SchemaObject
import openapiv3.SchemaObjectDef
import openapiv3.SchemaObjectRef

/**
 * @author kischn
 */
open class ModelDefinition(val name: String) {

    val fields: MutableList<Field> = ArrayList()

    /**
     * 在 openAPI v3 中的 ref 位置
     */
    private var componentId: String? = null

    /**
     * 字符串
     */
    fun string(
        name: String,
        remark: String? = null,
        init: (StringField.() -> Unit)? = null
    ) {
        val stringField = StringField(name, remark)
        init?.let { stringField.apply(it) }
        fields.add(stringField)
    }

    /**
     * Integer
     */
    fun int(name: String, remark: String? = null) {
        fields.add(IntegerField(name, remark))
    }

    /**
     * 日期类型
     */
    fun date(name: String, remark: String? = null) {
        fields.add(DateField(name, remark))
    }

    /**
     * 列表
     */
    fun intList(name: String, remark: String) {
        fields.add(IntListField(name, remark))
    }

    /**
     * 字符串列表
     */
    fun stringList(name: String, remark: String) {
        fields.add(StringListField(name, remark))
    }

    /**
     * 自定义对象列表
     */
    fun objList(name: String, remark: String, model: ModelDefinition) {
        fields.add(ModelListField(name, remark, model))
    }


    /**
     * 数据对象
     */
    fun obj(
        name: String,
        remark: String? = null,
        modelDef: ModelDefinition
    ) {
        fields.add(ModelDefinitionField(name, remark, modelDef))
    }

    /**
     * 直接是 id
     */
    fun id() {
        fields.add(IntegerField("id", "id").apply {
            nullable = false
        })
    }

    /**
     * 组件是否已经注册
     */
    fun componentRegistered(): Boolean = componentId != null

    protected open fun buildSchemaObject(title: String? = null): SchemaObjectDef {
        val properties = HashMap<String, SchemaObject>()
        fields.forEach { f ->
            properties[f.name] = f.toSchemaObject()
        }
        val requiredProperties = fields.filterNot { it.nullable }.map { it.name }.toList()
        return SchemaObjectDef(
            title = title,
            type = "object",
            properties = properties,
            required = requiredProperties.ifEmpty { null }
        )
    }

    fun toSchemaObject(title: String? = null): SchemaObject {

        if (componentId != null) {
            return SchemaObjectRef("#/components/schemas/$componentId")
        }

        return buildSchemaObject(title)
    }

    /**
     * 把自己注册到 openAPI v3 的 components 里面去
     */
    open fun registerComponents(apiV3: OpenAPIV3, currPackage: String) {
        val compId = "$currPackage.$name"
        apiV3.components.schemas[compId] = toSchemaObject()
        componentId = compId
    }
}

/**
 * 动态的,离散的,不会生成一个类的
 */
class DynamicModelDefinition : ModelDefinition("") {
    override fun registerComponents(apiV3: OpenAPIV3, currPackage: String) {
        // do nothing
    }
}

/**
 * 扁平的模型
 */
open class FlatModelDefinition : ModelDefinition("")

object FlatModel {

    /**
     * 原生类型都有啥
     */
    enum class FlatModelDataType(val openApiTypeName: String) {
        STRING("string"),
        INT("integer"),
        BOOLEAN("boolean"),
        FLOAT("number");
    }

    /**
     * 原生类型的
     */
    class PrimitiveModelDefinition(private val type: FlatModelDataType) : FlatModelDefinition() {
        override fun buildSchemaObject(title: String?): SchemaObjectDef {
            return SchemaObjectDef(
                title = title,
                type = type.openApiTypeName
            )
        }
    }

    /**
     * 原生类型的数组的
     */
    class PrimitiveListModelDefinition(private val primitiveType: FlatModelDataType) : FlatModelDefinition() {
        override fun buildSchemaObject(title: String?): SchemaObjectDef {
            return SchemaObjectDef(
                title = title,
                type = "array",
                items = SchemaObjectDef(
                    type = primitiveType.openApiTypeName
                )
            )
        }
    }

    /**
     * 对象 list
     */
    class ObjectListModelDefinition(private val model: ModelDefinition) : FlatModelDefinition() {
        override fun buildSchemaObject(title: String?): SchemaObjectDef {
            return SchemaObjectDef(
                title = title,
                type = "array",
                items = model.toSchemaObject()
            )
        }
    }

    fun string(): FlatModelDefinition {
        return PrimitiveModelDefinition(FlatModelDataType.STRING)
    }

    fun stringList(): FlatModelDefinition {
        return PrimitiveListModelDefinition(FlatModelDataType.STRING)
    }

    fun float(): FlatModelDefinition {
        return PrimitiveModelDefinition(FlatModelDataType.FLOAT)
    }

    fun floatList(): FlatModelDefinition {
        return PrimitiveListModelDefinition(FlatModelDataType.FLOAT)
    }

    fun boolean(): FlatModelDefinition {
        return PrimitiveModelDefinition(FlatModelDataType.BOOLEAN)
    }

    fun booleanList(): FlatModelDefinition {
        return PrimitiveListModelDefinition(FlatModelDataType.BOOLEAN)
    }

    fun int(): FlatModelDefinition {
        return PrimitiveModelDefinition(FlatModelDataType.INT)
    }

    fun intList(): FlatModelDefinition {
        return PrimitiveListModelDefinition(FlatModelDataType.INT)
    }

    fun objList(model: ModelDefinition): FlatModelDefinition {
        return ObjectListModelDefinition(model)
    }

}

/**
 * 可以定义成为实体的
 */
class EntityModelDefinition(name: String) : ModelDefinition(name)
