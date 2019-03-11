package com.assecopst.channels.devops.infrastructure


import com.assecopst.channels.devops.infrastructure.crawlers.MinionsFactory
import com.assecopst.channels.devops.infrastructure.crawlers.Worker
import com.assecopst.channels.devops.infrastructure.utils.Console

class ProjectsManager {

    private synchronized Set<String> crawledProjects

    ProjectsManager() {
        crawledProjects = []
    }

    void calcDependencies(Project aProject, Worker aObserver) {

        if (isAlreadyCrawled(aProject))
            return
        addCrawledProject(aProject)

        Console.debug("Launching FileParserMinion to calculate dependencies of Project $aProject.name:$aProject.version")
        MinionsFactory.create(MinionsFactory.Type.CRAWLER, aProject, aObserver)
    }

    private boolean isAlreadyCrawled(Project aProject) {

        String ref = getProjectRef(aProject)
        if (crawledProjects.contains(ref)) {
            Console.debug("Project '${ref}' was already crawled...")
            return true
        }
        crawledProjects << ref

        return false
    }

    private void addCrawledProject(Project aProject) {
        crawledProjects << getProjectRef(aProject)
    }

    private String getProjectRef(Project aProject) {
        return "$aProject.name:$aProject.version"
    }
}
