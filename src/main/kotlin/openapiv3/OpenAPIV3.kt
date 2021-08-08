package openapiv3

/**
 * 生成符合 OpenAPI v3 规范的描述文件.
 * https://spec.openapis.org/oas/v3.0.3#path-item-object
 *
 * @author kischn
 * @date 2021/8/7
 */

class OpenAPIV3(projectName: String) {
    val openapi = "3.0.3"
    val info = object {
        val title = projectName
        val version = "1.0"
    }
    val paths = LinkedHashMap<String, PathItemObject>()
    val components = ComponentsObject(HashMap())
}

// https://spec.openapis.org/oas/v3.0.3#schema-object
data class SchemaObject(
    val title: String? = null,
    val type: String,
    val properties: HashMap<String, SchemaObject>? = null,
    val format: String? = null,
    val items: SchemaObject? = null
)

// https://spec.openapis.org/oas/v3.0.3#components-object
data class ComponentsObject(
    val schemas: HashMap<String, SchemaObject>
)

// https://spec.openapis.org/oas/v3.0.3#parameter-object
data class ParameterObject(
    val `in`: String = "query",
    val name: String,
    val schema: SchemaObject,
    val required: Boolean = false
)

// https://spec.openapis.org/oas/v3.0.3#request-body-object
// 目前只实现 content key 为 application/json 的
data class RequestBodyObject(
    val required: Boolean,
    val content: HashMap<String, MediaTypeObject>
)

// https://spec.openapis.org/oas/v3.0.3#media-type-object
data class MediaTypeObject(
    val schema: SchemaObject
)

// https://spec.openapis.org/oas/v3.0.3#operation-object
data class OperationObject(
    val description: String,
    val parameters: List<ParameterObject>? = null,
    val requestBody: RequestBodyObject? = null,
    val responses: HashMap<Int, ResponseObject>? = null
)

// https://spec.openapis.org/oas/v3.0.3#response-object
data class ResponseObject(
    val description: String,
    val content: HashMap<String, MediaTypeObject>
)

// https://spec.openapis.org/oas/v3.0.3#path-item-object
typealias PathItemObject = LinkedHashMap<String, Any>