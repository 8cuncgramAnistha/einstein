package io.github.asseco.pst.infrastructure.utils

import com.vdurmont.semver4j.Semver
import io.github.asseco.pst.http.RepoExplorerFactory
import io.github.asseco.pst.infrastructure.Requirement
import io.github.asseco.pst.infrastructure.exceptions.VersionException

class SemanticVersion extends Semver {

    static final String SNAPSHOT_SUFFIX = "-SNAPSHOT"

    SemanticVersion(String aVersion, SemverType aType) {
        super(aVersion, aType)
    }


    /**
     * Creates a Semver version with Type = NPM
     *
     * @param aVersion
     * @return a SemanticVersion (extends Semver)
     */
    synchronized static SemanticVersion create(String aVersion) {

        SemanticVersion version

        try {

            version = new SemanticVersion(aVersion, SemverType.NPM)

        } catch (RuntimeException aException) {
            throw new VersionException("Unable to instantiate Semver version '$aVersion'", aException)
        }

        return version
    }


    /**
     * Checks if the provided version is a SNAPSHOT version
     *
     * @param aVersion
     * @return true if the provided @param represents a SNAPSHOT version
     */
    synchronized static boolean isSnapshot(String aVersion) {
        return hasSnapshotSuffix(aVersion)
    }

    static String getBiggestVersion(Set<SemanticVersion> aVersions) {

        return aVersions.stream().collect().max( { v1, v2 ->
            v1 <=> v2
        })
    }

    /**
     * Checks if given list of versions contains non compatible versions among them.
     *
     * @param aVersions
     * @return true if non compatible versions are found
     */
    synchronized static boolean hasNonCompatibleVersions(Set<SemanticVersion> aVersions) {

        SemanticVersion latestVer
        boolean foundNoncompatibleVersions = false

        aVersions.each {
            SemanticVersion version = (SemanticVersion) it

            if(latestVer) {
                if (version.diff(latestVer) == VersionDiff.MAJOR)
                    foundNoncompatibleVersions = true
            }
            latestVer = version
        }

        return foundNoncompatibleVersions
    }

    /**
     * Checks if the current object represents a SNAPSHOT version
     * Similar to @see SemanticVersion#isSnapshot(String aVersion)
     *
     * @return true if the current object is a SNAPSHOT version
     */
    boolean isSnapshot() {
        return hasSnapshotSuffix(this.toString())
    }

    /**
     * Finds a tag in the required project that satisfies the requirement versionRange.
     *
     * It does so by executing the following steps:
     *      * gets all tags from the project that satisfy the requirement versionRange
     *      * returns the highest version that satisfies the versionRange
     *
     * @param aRequirement
     * @return the version value
     */
    synchronized static String findSatisfyingVersion(Requirement aRequirement) {

        if(isSnapshot(aRequirement.versionRange))
            return aRequirement.versionRange

        List<String> tags = RepoExplorerFactory.get().listTags(
                aRequirement.getProjectNamespace(),
                aRequirement.getProjectName(),
                { tag ->
                    SemanticVersion version = create(tag.getName())
                    version.satisfies(aRequirement.getVersionRange())
                })

        Semver satisfies = tags.collect { create(it) }.max{ a, b ->
            (a <=> b)
        }

        return satisfies.getValue()
    }

    /**
     * Checks if provided version is greater, equal or lower than the current version <p>
     *
     * Semver specification does not deal with SNAPSHOT versions precedence as desired.<br>
     *
     * According to the lexical sorting of SemVer versions (with letter or hyphens), SNAPSHOT versions are considered
     * greater than alpha, beta or rc versions (i.e, <i>1.0.0-alpha < 1.0.0-beta < 1.0.0.rc < 1.0.0-SNAPSHOT < 1.0.0)</i>.
     *
     * However, since they represent the latest commit on the development branch, SNAPSHOT versions must be considered
     * the lowest version when compared with the above pre-release versions.<br>
     *
     * Therefore, this class overrides de Semver#compareTo() method in order to respect the
     * desired precedence: <i>1.0.0-SNAPSHOT < 1.0.0-alpha < 1.0.0-beta < 1.0.0-rc < 1.0.0<p></i>
     *
     * @see <a href="https://semver.org/#spec-item-11">Semver precedences</a>
     * @see <a href="https://tinyurl.com/y59lvzt8">com.vdurmont.semver4j.Semver#compareTo()</a>
     *
     * @param Semver aVersion
     * @return
     *   - 1 if current version is greater than provided version
     *   - 0 if both versions are equal
     *   - -1 if current version is lower than provided version
     *
     */
    @Override
    int compareTo(Semver aVersion) {

        SemanticVersion comparingVersion = (SemanticVersion) aVersion

        if(this.isSnapshot() || comparingVersion.isSnapshot())
            return compareWithSnapshot(comparingVersion)

        return super.compareTo(aVersion)
    }

    private int compareWithSnapshot(SemanticVersion aVersion) {

        int result = comparePrefixTo(aVersion)
        if(result == 0) {
            if(this.isSnapshot())
                return -1
            else
                return 1
        }
        else
            return result

    }

    /**
     * Gets the Prefix of the provided version<p>
     *
     * A version Prefix is composed by: Major, Minor and Patch <br>
     * (i.e, the Prefix of version 1.2.3-alpha.1+sha21fds32432 is 1.2.3) <p>
     *
     * @param aVersion
     * @return the version's prefix
     */
    private static String getPrefix(SemanticVersion aVersion) {
        return "${aVersion.getMajor()}.${aVersion.getMinor()}.${aVersion.getPatch()}"
    }

    private static boolean hasSnapshotSuffix(String aVersion) {
        return aVersion.trim().toUpperCase().contains(SNAPSHOT_SUFFIX)
    }

    private int comparePrefixTo(SemanticVersion aVersion) {

        SemanticVersion v1 = create(getPrefix(this))
        SemanticVersion v2 = create(getPrefix(aVersion))

        return (v1 <=> v2)
    }
}