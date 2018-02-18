package org.labun.jooq.generator.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.labun.jooq.generator.DefaultGenerator
import org.labun.jooq.generator.GenerationTool
import org.labun.jooq.generator.config.CodeGenerationConfig
import org.labun.jooq.generator.config.Configuration
import org.labun.jooq.generator.config.DatabaseConfig
import org.labun.jooq.generator.config.Defaults

/**
 * @author Konstantin Labun
 */
class JooqCodeGenerationPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def configuration = new Configuration()
        configuration.database = new DatabaseConfig()
        configuration.database.schemas(["PUBLIC"])

        JooqCodeGenerationExtension extension = project.extensions.create("jooqCodeGeneration", JooqCodeGenerationExtension.class)

        project.task('generateJOOQ') {
            doLast {
                configuration.database.with(extension.database)

                List<CodeGenerationConfig> defaultConfigs = Defaults.CodeGenerationConfigs.all()

                def sourceRootConfigurer = { it.generatedSourcesRoot = "${project.getBuildDir()}/generated/src/main/java/" }

                extension.generators.each({ cfg ->
                    def generator = new CodeGenerationConfig()
                    generator.with(sourceRootConfigurer)
                    generator.with(cfg)

                    def defaultGenerator = defaultConfigs.find({ it.generatorName == generator.generatorName })
                    if (defaultGenerator != null) {
                        generator = defaultGenerator
                        generator.with(sourceRootConfigurer)
                        generator.with(cfg)
                    }

                    configuration.codeGeneration.add(generator)
                })

                GenerationTool.generate(new DefaultGenerator(configuration))
            }
        }
    }
}