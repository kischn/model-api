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

    fun toSchemaObject(): SchemaObject {
        if (componentId != null) {
            return SchemaObjectRef("#/components/schemas/$componentId")
        }
        val properties = HashMap<String, SchemaObject>()
        fields.forEach { f ->
            properties[f.name] = f.toSchemaObject()
        }
        return SchemaObjectDef(type = "object", properties = properties)
    }

    /**
     * 把自己注册到 openAPI v3 的 components 里面去
     */
    fun registerComponents(apiV3: OpenAPIV3, currPackage: String) {
        val properties = HashMap<String, SchemaObject>()
        fields.forEach { f ->
            properties[f.name] = f.toSchemaObject()
        }
        val compId = "$currPackage.$name"
        apiV3.components.schemas[compId] = SchemaObjectDef(type = "object", properties = properties)
        componentId = compId
    }
}

/**
 * 动态的,离散的,不会生成一个类的
 */
class DynamicModelDefinition : ModelDefinition("")

/**
 * 扁平的模型
 */
open class FlatModelDefinition : ModelDefinition("")

object FlatModel {

    /**
     * 原生类型都有啥
     */
    enum class FlatModelDataType {
        STRING,
        INT,
        BOOLEAN,
        FLOAT
    }

    /**
     * 原生类型的
     */
    class PrimitiveModelDefinition(type: FlatModelDataType) : FlatModelDefinition()

    /**
     * 原生类型的数组的
     */
    class PrimitiveListModelDefinition(type: FlatModelDataType) : FlatModelDefinition()

    /**
     * 对象 list
     */
    class ObjectListModelDefinition(model: ModelDefinition) : FlatModelDefinition()

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
