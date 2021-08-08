/**
 * @author kischn
 * @date 2021/8/8
 */
import test.books.apis.bookApi
import base.path
import openapiv3.gen

fun main(args: Array<String>) {
    val rootApi = path("/api/v1", "me.kischn", "XX项目") {
        subApis(
            bookApi
        )
    }
    gen(rootApi)
}