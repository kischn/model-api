package base

import openapiv3.*

/**
 * @author kischn
 */
abstract class Api(val method: String, val path: String) {


    /**
     * 请求参数
     */
    open class Request(private val model: ModelDefinition) {
        open fun toParameters(): List<ParameterObject> {
            return model.fields.map { f ->
                ParameterObject(
                    `in` = "query",
                    name = f.name,
                    schema = f.toSchemaObject(),
                    required = !f.nullable
                )
            }
        }

        open fun toRequestBody(): RequestBodyObject {
            return RequestBodyObject(
                required = true,
                content = hashMapOf("application/json" to MediaTypeObject(model.toSchemaObject()))
            )
        }
    }

    /**
     * 动态的 request.
     * 不会生成一个类.
     */
    class DynamicRequest() : Request(ModelDefinition("_empty"))

    /**
     * 分页查询的请求
     */
    class PageRequest(private val model: ModelDefinition) : Request(model) {
        override fun toParameters(): List<ParameterObject> {
            val parameters = ArrayList<ParameterObject>()
            model.fields.forEach { f ->
                parameters.add(
                    ParameterObject(
                        `in` = "query",
                        name = f.name,
                        schema = f.toSchemaObject(),
                        required = !f.nullable
                    )
                )
            }
            parameters.add(
                ParameterObject(
                    `in` = "query",
                    name = "pageSize",
                    schema = SchemaObjectDef(
                        type = "integer",
                        format = "int32"
                    ),
                    required = true
                )
            )
            parameters.add(
                ParameterObject(
                    `in` = "query",
                    name = "p",
                    schema = SchemaObjectDef(
                        type = "integer",
                        format = "int32"
                    ),
                    required = true
                )
            )
            return parameters
        }
    }

    /**
     * 直接 body 请求的
     */
    class BodyRequest(private val model: ModelDefinition) : Request(model) {

        override fun toRequestBody(): RequestBodyObject {
            return RequestBodyObject(
                required = false,
                content = hashMapOf("application/json" to MediaTypeObject(model.toSchemaObject()))
            )
        }
    }


    /**
     * bare 的响应
     */
    open class Response(private val model: ModelDefinition) {
        open fun toMediaTypeObject(): MediaTypeObject {
            return MediaTypeObject(model.toSchemaObject())
        }
    }

    /**
     * 默认的 code/message/data 封装
     */
    class WrappedResponse(private val model: ModelDefinition) : Response(model) {
        override fun toMediaTypeObject(): MediaTypeObject {
            val wrappedProperties = hashMapOf(
                "code" to SchemaObjectDef(type = "integer", format = "int32"),
                "message" to SchemaObjectDef(type = "string"),
                "data" to model.toSchemaObject()
            )
            return MediaTypeObject(SchemaObjectDef(type = "object", properties = wrappedProperties, title = model.name))
        }
    }

    /**
     * 分页的响应
     */
    class PagedResponse(private val model: ModelDefinition) : Response(model) {

        override fun toMediaTypeObject(): MediaTypeObject {
            val wrappedProperties: HashMap<String, SchemaObject> = hashMapOf(
                "code" to SchemaObjectDef(type = "integer", format = "int32"),
                "message" to SchemaObjectDef(type = "string"),
                "data" to SchemaObjectDef(
                    type = "object",
                    properties = hashMapOf(
                        "totalCount" to SchemaObjectDef(type = "integer", format = "int32"),
                        "totalPage" to SchemaObjectDef(type = "integer", format = "int32"),
                        "pageSize" to SchemaObjectDef(type = "integer", format = "int32"),
                        "list" to SchemaObjectDef(type = "array", items = model.toSchemaObject())
                    )
                )
            )
            return MediaTypeObject(SchemaObjectDef(type = "object", properties = wrappedProperties))
        }
    }

    /**
     * 查询参数
     */
    protected var req: Request? = null

    /**
     * 返回参数
     */
    private var resp: Response? = null

    /**
     * 注释
     */
    var description: String = ""

    fun description(description: String) {
        this.description = description
    }

    fun req(model: ModelDefinition) {
        req = Request(model)
    }

    fun reqBody(model: ModelDefinition) {
        req = BodyRequest(model)
    }

    fun reqBody(init: DynamicModelDefinition.() -> Unit) {
        req = BodyRequest(DynamicModelDefinition().apply(init))
    }

    fun req(init: DynamicModelDefinition.() -> Unit) {
        req = Request(DynamicModelDefinition().apply(init))
    }

    fun pageReq(model: ModelDefinition) {
        req = PageRequest(model)
    }

    fun pageReq(init: ModelDefinition.() -> Unit) {
        req = PageRequest(DynamicModelDefinition().apply(init))
    }

    fun resp(model: ModelDefinition) {
        resp = Response(model)
    }

    fun wrappedResp(model: ModelDefinition) {
        resp = WrappedResponse(model)
    }

    fun wrappedResp(init: FlatModel.() -> FlatModelDefinition) {
        resp = WrappedResponse(FlatModel.init())
    }

    open fun pageResp(model: ModelDefinition) {
        resp = PagedResponse(model)
    }

    abstract fun fillPathItemObject(pathItemObject: PathItemObject)

    open fun getResponseObject(description: String): ResponseObject {
        val content = HashMap<String, MediaTypeObject>()
        resp?.let {
            content["application/json"] = it.toMediaTypeObject()
        }
        return ResponseObject(description = description, content = content)
    }
}

class PutApi(path: String) : Api("PUT", path) {
    override fun fillPathItemObject(pathItemObject: PathItemObject) {
        pathItemObject["put"] = OperationObject(
            description = super.description,
            requestBody = req?.toRequestBody(),
            responses = hashMapOf(200 to getResponseObject(description + "成功响应结果"))
        )
    }
}

class PostApi(path: String) : Api("POST", path) {
    override fun fillPathItemObject(pathItemObject: PathItemObject) {
        pathItemObject["post"] = OperationObject(
            description = super.description,
            requestBody = req?.toRequestBody(),
            responses = hashMapOf(200 to getResponseObject(description + "成功响应结果"))
        )
    }
}

class GetApi(path: String) : Api("GET", path) {
    override fun fillPathItemObject(pathItemObject: PathItemObject) {
        pathItemObject["get"] = OperationObject(
            description = super.description,
            parameters = super.req?.toParameters() ?: emptyList(),
            responses = hashMapOf(200 to getResponseObject(description + "成功响应结果"))
        )
    }
}

class DeleteApi(path: String) : Api("DELETE", path) {
    override fun fillPathItemObject(pathItemObject: PathItemObject) {
        pathItemObject["get"] = OperationObject(
            description = super.description,
            parameters = super.req?.toParameters() ?: emptyList(),
            responses = hashMapOf(200 to getResponseObject(description + "成功响应结果"))
        )
    }
}

fun path(
    path: String,
    pkg: String,
    name: String,
    init: PathDefinition.() -> Unit
): PathDefinition {
    return PathDefinition(path, pkg, name).apply(init)
}

class PathDefinition(
    val path: String,
    val pkg: String = path,
    val name: String = ""
) {
    /**
     * 定义的 接口
     */
    val apis: MutableList<Api> = ArrayList()

    /**
     * 当前路径下定义的模型
     */
    val models: MutableList<ModelDefinition> = ArrayList()

    /**
     * 下级路径
     */
    val children: MutableList<PathDefinition> = ArrayList()

    fun get(path: String = "", init: GetApi.() -> Unit) {
        apis.add(GetApi(path).apply(init))
    }

    fun post(path: String = "", init: PostApi.() -> Unit) {
        apis.add(PostApi(path).apply(init))
    }

    fun put(path: String = "", init: PutApi.() -> Unit) {
        apis.add(PutApi(path).apply(init))
    }

    fun delete(path: String = "", init: DeleteApi.() -> Unit) {
        apis.add(DeleteApi(path).apply(init))
    }

    fun subApis(vararg path: PathDefinition) {
        children.addAll(path)
    }

    fun model(name: String, init: ModelDefinition.() -> Unit): ModelDefinition {
        return ModelDefinition(name).apply(init).also(models::add)
    }

    fun entity(name: String, init: EntityModelDefinition.() -> Unit): ModelDefinition {
        return EntityModelDefinition(name).apply(init).also(models::add)
    }
}