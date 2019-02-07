package com.asseco.pst.devops.infrastructure.version

import java.util.regex.Matcher

class SemanticVersion extends Version {

    int major, minor, patch

    protected SemanticVersion() {}

    SemanticVersion(String aVersionStr) {
        super(aVersionStr)
    }

    @Override
    protected void parse() {

        major = formatNumber(tokenizedVersion[0])
        minor = formatNumber(tokenizedVersion[1])
        patch = formatNumber(tokenizedVersion[2])
    }

    @Override
    boolean match(String aVersion) {
        return ((Matcher) (aVersion =~ /([0-9]+\.[0-9]+\.[0-9]+)/)).matches()
    }

    @Override
    String getGitMatchVersionExp() {
        return "${major}.*"
    }

    @Override
    String getGitMatchRcVersion() {
        return "${major}.${minor}*rc*"
    }

    @Override
    def getVersionRegexExp() {
        def exp = /^(.)*?(${major}\.[0-9]+\.[0-9])/
        return exp
    }

    @Override
    def getRcRegexExp() {
        def exp = /^(.)*?(${major}\.${minor}\.[0-9]+-rc\..)/
        return exp
    }
}
