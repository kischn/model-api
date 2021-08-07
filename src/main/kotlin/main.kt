import apis.bookApi
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