package codegen

import base.PathDefinition

/**
 * 后端代码生成器
 * @author kischn
 * @date 2021/8/10
 */
class CodeGenerator {

    /**
     * 生成相关代码
     */
    fun generateCode(basePath: String, basePkg: String, pathDefinitions: List<PathDefinition>) {

        for (pathDefinition in pathDefinitions) {
            doGenerateCode(basePath, basePkg, pathDefinition)
        }
    }

    private fun doGenerateCode(parentPath: String, parentPackage: String, pathDefinition: PathDefinition) {
        val currPath = parentPath + pathDefinition.path
        val currPackage =
            if (parentPackage.isEmpty()) pathDefinition.pkg else (parentPackage + "." + pathDefinition.pkg)
        // 生成 entity
        pathDefinition.models.forEach { modelDefinition -> }
        // 生成 controller
        // 生成 service
        // 生成 repository
        // 递归
        TODO("Not yet implemented")
    }
}

