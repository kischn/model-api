/**
 * @author kischn
 * @date 2021/8/8
 */
import base.path
import openapiv3.genOpenAPIV3JSON
import test.books.apis.bookApi

fun main(args: Array<String>) {
    val rootApi = path("/api/v1", "me.kischn", "XX项目") {
        subApis(
            bookApi
        )
    }
    val json = genOpenAPIV3JSON(rootApi)
    println(json)
}