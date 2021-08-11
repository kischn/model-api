package test.books.apis

import base.PathDefinition
import base.path
import java.util.regex.Pattern

/**
 * 图书模块
 * @author kischn
 */

/**
 * 图书相关接口
 */
fun bookApi() = path("book", "书籍相关") {

    /**
     * 作者模型
     */
    val authorModel = model("Author") {
        id()
        string("name", "姓名")
    }

    /**
     * 人员和章节 DTO
     */
    val userAndSection = entity("UserAndSection") {
        id()
        string("name", "姓名")
        string("section", "贡献的章节")
    }

    /**
     * 书籍模型
     */
    val bookModel = entity("Book") {
        id()
        string("isbn", "ISBN号") {
            nullable = false
        }
        string("name", "名称") {
            size = 1..10
            nullable = false
            pattern = Pattern.compile("^\\w{1,10}$")
        }
        date("publishDate", "发布日期")
        // 对象
        obj("author", "作者", authorModel)
        // 字符串列表
        stringList("words", "关键词")
        // 自定义对象列表
        objList("thanks", "致谢", userAndSection)
    }

    get {
        description("查看书籍详情")
        req {
            id()
        }
        // 仅包一层, code/message/data
        wrappedResp(bookModel)
    }

    get("/s") {
        description("分页查询")
        // 分页的请求,就是自动添加了 p,pageSize
        pageReq {
            string("name", "名称")
        }
        // 分页的响应,把指定模型外面再包两层,一层 code/message 里面再一层分页的
        pageResp(bookModel)
    }

    post {
        description("保存")
        reqBody(bookModel)
        wrappedResp(bookModel)
    }

    put {
        description("更新")
        reqBody(bookModel)
        wrappedResp(bookModel)
    }

    put("/disable") {
        description("下架图书")
        req {
            id()
        }
        wrappedResp {
            intList()
        }
    }

    delete {
        description = "删除图书"
        req { id() }
        wrappedResp {
            string()
        }
    }

    // 加入到子章节里面
    subPaths(bookSection())
}

/**
 * 书籍章节
 */
fun bookSection(): PathDefinition = path("section", "章节") {
    val bookSection = entity("BookSection") {
        id()
        string("sectionName") {
            nullable = false
        }
        int("contentLength")
    }

    get {
        description = "章节详情"
        req { id() }
        wrappedResp(bookSection)
    }

    get("/s") {
        description = "章节查询"
        req {
            id()
        }
        pageResp(bookSection)
    }
}