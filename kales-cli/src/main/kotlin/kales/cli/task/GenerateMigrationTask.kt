package kales.cli.task

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import kales.cli.writeTextWithLogging
import kales.migrations.Migration
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

class GenerateMigrationTask(
    workingDir: File,
    private val migrationClassName: String,
    private val dateProvider: () -> Date = { Date() }
) : KalesContextualTask(workingDir) {
  override fun run() {
    val timestamp = SimpleDateFormat("yyyyMMddhhmmss").format(dateProvider())
    val migrationTypeSpec = TypeSpec.classBuilder(migrationClassName)
        .superclass(Migration::class)
        .addFunction(FunSpec.builder("up")
            .addModifiers(KModifier.OVERRIDE)
            .build())
        .addFunction(FunSpec.builder("down")
            .addModifiers(KModifier.OVERRIDE)
            .build())
        .build()
    val fileSpec = FileSpec.builder("$appPackageName.db.migrate", migrationClassName)
        .addType(migrationTypeSpec)
        .build()
    val outputPath = dbMigrateDir.toPath().resolve("M${timestamp}_$migrationClassName.kt")
    ByteArrayOutputStream().use { baos ->
      OutputStreamWriter(baos, StandardCharsets.UTF_8).use { writer ->
        fileSpec.writeTo(writer)
      }
      // We need to instantiate the migration class name at the bottom of the migration file so that
      // the Kotlin script engine will return that type from eval() and be able to execute the
      // migration. Without that, eval() returns `null` and we cannot run it. There must be a better
      // way to do this...
      val finalText = "$baos\n$migrationClassName()\n"
      outputPath.toFile().writeTextWithLogging(finalText)
    }
  }
}