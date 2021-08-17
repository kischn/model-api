import codegen.ClassField
import codegen.ClassInfo

/**
 * @author kischn
 * @date 2021/8/12
 */

fun main() {
    val classInfo = ClassInfo("com.sogata.test", "Book")
    classInfo.extImports.addAll(arrayOf("import java.util.List", "import java.sql.Timestamp"))
    classInfo.annotations.apply {
        add("@Entity")
        add("@Table(name=\"jack\")")
    }
    classInfo.fields.add(ClassField("name", "String").apply {
        annotations.add("@NotNull")
        annotations.add("@Id")
    })

    print(classInfo.buildClassFile())
}