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
                    required = !f.nullable,
                    description = f.remark
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
                        required = !f.nullable,
                        description = f.remark
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
                    required = true,
                    description = "页大小"
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
                    required = true,
                    description = "当前页"
                )
            )
            return parameters
        }
    }

    /**
     * 直接 body 请求的
     */
    class BodyRequest(private val model: ModelDefinition) : Request(model) {

        override fun toParameters(): List<ParameterObject> {
            return emptyList()
        }

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
            val wrappedProperties = linkedMapOf(
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
            val wrappedProperties: HashMap<String, SchemaObject> = linkedMapOf(
                "code" to SchemaObjectDef(type = "integer", format = "int32"),
                "message" to SchemaObjectDef(type = "string"),
                "data" to SchemaObjectDef(
                    type = "object",
                    properties = linkedMapOf(
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

    fun pageResp(model: ModelDefinition) {
        resp = PagedResponse(model)
    }

    abstract fun getOperationObject(): Pair<String, OperationObject>

    open fun getResponseObject(description: String): ResponseObject {
        return ResponseObject(
            description = description,
            content = resp?.let { hashMapOf("application/json" to it.toMediaTypeObject()) } ?: hashMapOf()
        )
    }
}

/**
 * 基于查询参数的 api .如 get/delete
 */
abstract class ParameterBasedApi(method: String, path: String) : Api(method, path) {

    override fun getOperationObject(): Pair<String, OperationObject> {
        return method to OperationObject(
            description = super.description,
            parameters = super.req?.toParameters() ?: emptyList(),
            responses = hashMapOf(200 to getResponseObject(description))
        )
    }

}

/**
 * 基于请求体的 api 如 post/put
 */
abstract class RequestBodyBasedApi(method: String, path: String) : Api(method, path) {
    /**
     * 查询参数
     */
    private var reqBody: BodyRequest? = null

    fun reqBody(model: ModelDefinition) {
        reqBody = BodyRequest(model)
    }

    override fun getOperationObject(): Pair<String, OperationObject> {
        return method to OperationObject(
            description = super.description,
            parameters = req?.toParameters(),
            requestBody = reqBody?.toRequestBody(),
            responses = hashMapOf(200 to getResponseObject(description))
        )
    }
}

class PutApi(path: String) : RequestBodyBasedApi("put", path)

class PostApi(path: String) : RequestBodyBasedApi("post", path)

class GetApi(path: String) : ParameterBasedApi("get", path)

class DeleteApi(path: String) : ParameterBasedApi("delete", path)

fun path(
    name: String,
    description: String,
    init: PathDefinition.() -> Unit
): PathDefinition {
    return PathDefinition("/$name", name.lowercase(), description).apply(init)
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

    fun subPaths(vararg path: PathDefinition) {
        children.addAll(path)
    }

    fun model(name: String, init: ModelDefinition.() -> Unit): ModelDefinition {
        return ModelDefinition(name).apply(init).also(models::add)
    }

    fun entity(name: String, init: EntityModelDefinition.() -> Unit): ModelDefinition {
        return EntityModelDefinition(name).apply(init).also(models::add)
    }
}