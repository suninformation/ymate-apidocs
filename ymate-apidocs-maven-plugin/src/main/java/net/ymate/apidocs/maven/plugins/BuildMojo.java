/*
 * Copyright 2007-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.apidocs.maven.plugins;

import net.ymate.apidocs.Docs;
import net.ymate.apidocs.IDocs;
import net.ymate.apidocs.annotation.Api;
import net.ymate.apidocs.base.DocInfo;
import net.ymate.apidocs.render.JsonDocRender;
import net.ymate.apidocs.render.MarkdownDocRender;
import net.ymate.apidocs.render.PostmanDocRender;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.impl.DefaultBeanLoader;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 基于当前工程或指定的JSON格式的ApiDocs配置构建文档，支持html、json或markdown等文件格式输出
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/02 22:09
 */
@Mojo(name = "build", requiresDependencyResolution = ResolutionScope.RUNTIME, requiresDependencyCollection = ResolutionScope.RUNTIME)
@Execute(phase = LifecyclePhase.COMPILE)
public class BuildMojo extends AbstractMojo {

    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    private MavenProject mavenProject;

    @Parameter(property = "packageNames", defaultValue = "${project.groupId}")
    private String[] packageNames;

    /**
     * 输出格式: html|json|markdown, 默认值: markdown
     */
    @Parameter(property = "format", defaultValue = "markdown")
    private String format;

    @Parameter(property = "outputDir", defaultValue = "${basedir}")
    private String outputDir;

    /**
     * 自定义语言
     */
    @Parameter(property = "language")
    private String language;

    /**
     * 是否覆盖已存在的文件
     */
    @Parameter(property = "overwrite")
    private boolean overwrite;

    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {
        try (IApplication application = YMP.run()) {
            if (StringUtils.isNotBlank(language)) {
                application.getI18n().current(LocaleUtils.toLocale(language));
            }
            List<URL> urls = new ArrayList<>();
            urls.add(new File(mavenProject.getBuild().getOutputDirectory()).toURI().toURL());
            for (Artifact dependency : mavenProject.getArtifacts()) {
                urls.add(dependency.getFile().toURI().toURL());
            }
            IBeanLoader beanLoader = new DefaultBeanLoader();
            beanLoader.setClassLoader(new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader()));
            beanLoader.registerPackageNames(Arrays.asList(packageNames));
            //
            getLog().info(String.format("packageNames: %s", beanLoader.getPackageNames()));
            getLog().info(String.format("outputDir: %s", outputDir));
            //
            IDocs docs = application.getModuleManager().getModule(Docs.class);
            for (Class<?> clazz : beanLoader.load()) {
                if (clazz.isAnnotationPresent(Api.class)) {
                    getLog().info(String.format("Scanned to: %s", clazz.getName()));
                    docs.registerApi((Class<? extends Api>) clazz);
                }
            }
            if (docs.getDocs().isEmpty()) {
                getLog().warn("No documents found.");
            } else {
                switch (StringUtils.lowerCase(format)) {
                    case "html":
                        writeToHtml(docs);
                        break;
                    case "postman":
                        writeToPostman(docs);
                        break;
                    case "json":
                        writeToJson(docs);
                        break;
                    case "markdown":
                        writeToMarkdown(docs);
                        break;
                    default:
                        getLog().warn(String.format("Output in %s format is not supported.", format));
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }

    private File getCheckedTargetFile(String filePath) {
        File targetFile = new File(outputDir, filePath);
        boolean notSkipped = !targetFile.exists() || targetFile.exists() && overwrite;
        if (notSkipped) {
            File parentFile = targetFile.getParentFile();
            if (parentFile.exists() || parentFile.mkdirs()) {
                return targetFile;
            }
        } else {
            getLog().warn("Skip existing file " + targetFile);
        }
        return null;
    }

    private void writeToHtml(IDocs docs) throws IOException {
        // TODO Write to HTML.
    }

    private void writeToPostman(IDocs docs) throws IOException {
        for (DocInfo docInfo : docs.getDocs().values()) {
            File targetFile = getCheckedTargetFile(String.format("docs/postman_collection_%s.json", docInfo.getId()));
            if (targetFile != null) {
                try (OutputStream outputStream = new FileOutputStream(targetFile)) {
                    new PostmanDocRender(docInfo).render(outputStream);
                    this.getLog().info("Output file: " + targetFile);
                }
            }
        }
    }

    private void writeToMarkdown(IDocs docs) throws IOException {
        for (DocInfo docInfo : docs.getDocs().values()) {
            File targetFile = getCheckedTargetFile(String.format("docs/%s.md", docInfo.getId()));
            if (targetFile != null) {
                try (OutputStream outputStream = new FileOutputStream(targetFile)) {
                    new MarkdownDocRender(docInfo).render(outputStream);
                    this.getLog().info("Output file: " + targetFile);
                }
            }
        }
    }

    private void writeToJson(IDocs docs) throws IOException {
        for (DocInfo docInfo : docs.getDocs().values()) {
            File targetFile = getCheckedTargetFile(String.format("docs/%s.json", docInfo.getId()));
            if (targetFile != null) {
                try (OutputStream outputStream = new FileOutputStream(targetFile)) {
                    new JsonDocRender(docInfo).render(outputStream);
                    this.getLog().info("Output file: " + targetFile);
                }
            }
        }
    }
}
