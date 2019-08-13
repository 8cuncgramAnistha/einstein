package com.pst.asseco.channels.devops.infrastructure.crawlers

import com.pst.asseco.channels.devops.infrastructure.Einstein
import com.pst.asseco.channels.devops.infrastructure.Project
import com.pst.asseco.channels.devops.infrastructure.utils.Console

class FileParserMinion extends Crawler {


    FileParserMinion(Project aProject) {
        super(aProject)
    }

    @Override
    void work() {
        checkProjectDependencies()
        Einstein.addScannedProject(project)
    }

    private void checkProjectDependencies() {

        Console.print("\nChecking dependencies of Project '$project.ref':")

        if (project.hasRequirementsFile()) {
            if (Einstein.isDebugModeOn())
                storeFile()
            parseRequirements()
        } else {
            Console.warn("Project '$project.ref' doesn't have a ${Project.EINSTEIN_FILENAME} file...")
        }
    }

    private void parseRequirements() {
        project.readRequirements().each { requirement ->
            MinionsFactory.create(MinionsFactory.Type.VERSION_SEEKER, project, this, requirement)
        }
    }
}
