package org.tyrannyofheaven.bukkit.util;

/**
 * Holder for specific fields in the manifest. Specifically:
 * <ul>
 *   <li>artifactId (Implementation-Title)</li>
 *   <li>version (Implementation-Version)</li>
 *   <li>build (Implementation-Build)</li>
 * </ul>
 * 
 * @author asaddi
 */
public class VersionInfo {

    private final String artifactId;

    private final String version;

    private final String build;

    private final String versionString;

    private final String fullVersion;

    public VersionInfo(String artifactId, String version, String build) {
        this.artifactId = artifactId;
        this.version = version;
        this.build = build;

        if (version.contains("SNAPSHOT"))
            versionString = String.format("%s (build: %s)", version, build);
        else
            versionString = version;
        fullVersion = String.format("%s %s", artifactId, versionString);
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getBuild() {
        return build;
    }

    public String getVersionString() {
        return versionString;
    }

    public String getFullVersion() {
        return fullVersion;
    }

}
