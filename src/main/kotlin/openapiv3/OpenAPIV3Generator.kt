package openapiv3

import base.PathDefinition
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * @author kischn
 * @date 2021/8/7
 */

fun genOpenAPIV3JSON(pathDefinition: PathDefinition):String {
    val apiV3 = OpenAPIV3(pathDefinition.name)
    addPath(
        apiV3,
        "",
        "",
        pathDefinition
    )
    val objectMapper = ObjectMapper()
    // 默认忽略 null 值的属性
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return objectMapper.writeValueAsString(apiV3)
}

fun addPath(
    apiV3: OpenAPIV3,
    parentPackage: String,
    parentPath: String,
    pathDefinition: PathDefinition
) {
    // 先处理 components
    val currPackage = if (parentPackage.isEmpty()) pathDefinition.pkg else parentPackage + "." + pathDefinition.pkg
    pathDefinition.models.forEach { m ->
        m.registerComponents(apiV3, currPackage)
    }

    // 再处理接口
    val paths = apiV3.paths
    val currPath = parentPath + pathDefinition.path
    pathDefinition.apis
        .groupBy { api -> api.path }
        .forEach { (path, apiList) ->
            val pathItemObject = PathItemObject()
            apiList.forEach { api -> api.fillPathItemObject(pathItemObject) }
            // 写入到 paths 里面
            paths[currPath + path] = pathItemObject
        }

    // 检查自己有没有 subApi 有则递归
    for (childPathDefinition in pathDefinition.children) {
        addPath(
            apiV3,
            currPackage,
            currPath,
            childPathDefinition
        )
    }
}