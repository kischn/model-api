/**
 * @author kischn
 * @date 2021/8/8
 */
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import openapiv3.OpenAPIV3
import openapiv3.ProjectInfo
import test.books.apis.bookApi

fun main(args: Array<String>) {

    val apiV3 = OpenAPIV3(ProjectInfo("XX项目", "1.0.1"))
    apiV3.addPath(
        "me.kischn",
        "/api/v1",
        listOf(bookApi())
    )

    val objectMapper = ObjectMapper()
    // 默认忽略 null 值的属性
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    // 生成 JSON
    val json = objectMapper.writeValueAsString(apiV3)

    println(json)
}