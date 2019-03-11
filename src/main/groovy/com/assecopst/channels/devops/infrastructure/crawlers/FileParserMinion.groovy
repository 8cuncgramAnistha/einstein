package com.assecopst.channels.devops.infrastructure.crawlers

import com.assecopst.channels.devops.infrastructure.Einstein
import com.assecopst.channels.devops.infrastructure.Project
import com.assecopst.channels.devops.infrastructure.utils.Console

class FileParserMinion extends Crawler {


    FileParserMinion(Project aProject) {
        super(aProject)
    }

    @Override
    void work() {
        checkProjectDependencies()
        Einstein.getScannedDependencies().add(project)
    }

    private void checkProjectDependencies() {

        Console.print("Project '$project.name:$project.version' - Checking dependencies...")

        if (project.hasRequirementsFile()) {
            if (Einstein.properties.isDebugModeOn())
                storeFile() // store file for debug purposes
            parseRequirements()
        } else {
            Console.warn("Project '$project.name:$project.version' doesn't have a requirements file...")
        }
    }

    private void parseRequirements() {

        project.getRequirementsFileContent().eachLine { dependencyLine ->
            String line = dependencyLine.trim()
            if (!line)
                return

            MinionsFactory.create(MinionsFactory.Type.VERSION_SEEKER, project, this, line)
        }
    }
}
