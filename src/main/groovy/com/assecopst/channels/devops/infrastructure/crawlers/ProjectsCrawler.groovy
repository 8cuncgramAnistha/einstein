package com.assecopst.channels.devops.infrastructure.crawlers

import com.assecopst.channels.devops.infrastructure.Einstein
import com.assecopst.channels.devops.infrastructure.Project
import com.assecopst.channels.devops.infrastructure.utils.Console


class ProjectsCrawler extends Worker {

    List<Project> projects

    ProjectsCrawler(List<Project> aProjects) {
        super()
        _id = "projects.crawler"
        projects = aProjects
    }

    @Override
    void work() {
        calcDependencies()
    }

    private void calcDependencies() {

        projects.each { project ->

            Einstein.getDpManager().addDependency(project.name, project.version)
            Einstein.getProjectsManager().calcDependencies(project, this)
            Console.debug("Calculations of Project '$project.name' dependencies started...")
        }
    }
}