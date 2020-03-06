package org.ods.usecase

import com.cloudbees.groovy.cps.NonCPS

import org.ods.parser.JUnitParser
import org.ods.util.IPipelineSteps
import org.ods.util.Project

class JUnitTestReportsUseCase {

    private Project project
    private IPipelineSteps steps

    JUnitTestReportsUseCase(Project project, IPipelineSteps steps) {
        this.project = project
        this.steps = steps
    }

    @NonCPS
    List<File> loadTestReportsFromPath(String path) {
        def result = []

        try {
            new File(path).traverse(nameFilter: ~/.*\.xml$/, type: groovy.io.FileType.FILES) { file ->
                result << file
            }
        } catch (FileNotFoundException e) {}

        return result
    }

    Map parseTestReportFiles(List<File> files) {
        def result = [ testsuites: [] ]

        files.each { file ->
            def testResult = JUnitParser.parseJUnitXML(file.text)
            result.testsuites.addAll(testResult.testsuites)
        }

        return result
    }

    void reportTestReportsFromPathToJenkins(String path) {
        this.steps.junit("${path}/**/*.xml")
    }
}
