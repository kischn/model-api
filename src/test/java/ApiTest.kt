/**
 * @author kischn
 * @date 2021/8/8
 */
import base.path
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import openapiv3.OpenAPIV3
import openapiv3.ProjectInfo
import test.books.apis.bookApi

fun main(args: Array<String>) {
    val rootApi = path("/api/v1", "me.kischn", "XX项目根接口") {
        subApis(
            bookApi
        )
    }

    val apiV3 = OpenAPIV3(ProjectInfo("XX项目", "1.0.1"))
    apiV3.addPath(
        "",
        "",
        rootApi
    )

    val objectMapper = ObjectMapper()
    // 默认忽略 null 值的属性
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    // 生成 JSON
    val json = objectMapper.writeValueAsString(apiV3)

    println(json)
}