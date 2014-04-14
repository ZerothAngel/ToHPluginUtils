/*
 * Largely based off Mojang's AccountsClient code.
 * https://github.com/Mojang/AccountsClient
 */
package org.tyrannyofheaven.bukkit.util.uuid;

import static org.tyrannyofheaven.bukkit.util.ToHStringUtils.hasText;
import static org.tyrannyofheaven.bukkit.util.uuid.UuidUtils.uncanonicalizeUuid;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Lists;

public class MojangUuidResolver implements UuidResolver {

    private static final String AGENT = "minecraft";

    private static final UuidDisplayName NULL_UDN = new UuidDisplayName(UUID.randomUUID(), "NOT FOUND");

    private final Cache<String, UuidDisplayName> cache;

    public MojangUuidResolver(int cacheMaxSize, long cacheTtl, TimeUnit cacheTtlUnits) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(cacheMaxSize)
                .expireAfterWrite(cacheTtl, cacheTtlUnits)
                .build(new CacheLoader<String, UuidDisplayName>() {
                    @Override
                    public UuidDisplayName load(String key) throws Exception {
                        UuidDisplayName udn = _resolve(key);
                        return udn != null ? udn : NULL_UDN; // Doesn't like nulls, so we use a marker object instead
                    }
                });
    }

    @Override
    public UuidDisplayName resolve(String username) {
        if (!hasText(username))
            throw new IllegalArgumentException("username must have a value");

        try {
            UuidDisplayName udn = cache.get(username.toLowerCase());
            return udn != NULL_UDN ? udn : null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UuidDisplayName resolve(String username, boolean cacheOnly) {
        if (!hasText(username))
            throw new IllegalArgumentException("username must have a value");

        if (cacheOnly) {
            UuidDisplayName udn = cache.asMap().get(username.toLowerCase());
            if (udn == null) return null;
            return udn != NULL_UDN ? udn : null; // NB Can't tell between "not cached" and "maps to null"
        }
        else return resolve(username); // Same as normal version
    }

    @Override
    public Map<String, UuidDisplayName> resolve(Collection<String> usernames) throws Exception {
        if (usernames == null)
            throw new IllegalArgumentException("usernames cannot be null");

        Map<String, UuidDisplayName> result = new LinkedHashMap<String, UuidDisplayName>();

        final int BATCH_SIZE = 100;
        final int MAX_PAGES = BATCH_SIZE; // The off chance that it returns 1 per page

        for (List<String> sublist : Lists.partition(new ArrayList<String>(usernames), BATCH_SIZE)) {
            List<ProfileCriteria> criteriaList = new ArrayList<ProfileCriteria>(sublist.size());
            for (String username : sublist) {
                criteriaList.add(new ProfileCriteria(username, AGENT));
            }
            ProfileCriteria[] criteria = criteriaList.toArray(new ProfileCriteria[criteriaList.size()]);

            for (int page = 1; page <= MAX_PAGES; page++) {
                ProfileSearchResult searchResult = searchProfiles(page, criteria);
                if (searchResult.getSize() == 0) break;
                for (int i = 0; i < searchResult.getSize(); i++) {
                    String username = searchResult.getProfiles().get(i).getName();
                    UUID uuid = uncanonicalizeUuid(searchResult.getProfiles().get(i).getId());
                    result.put(username.toLowerCase(), new UuidDisplayName(uuid, username));
                }
            }
        }

        return result;
    }

    private UuidDisplayName _resolve(String username) throws IOException, ParseException {
        if (!hasText(username))
            throw new IllegalArgumentException("username must have a value");

        ProfileSearchResult result = searchProfiles(1, new ProfileCriteria(username, AGENT));

        if (result.getSize() < 1) return null;

        // TODO what to do if there are >1?
        Profile p = result.getProfiles().get(0);

        String uuidString = p.getId();
        UUID uuid;
        try {
            uuid = uncanonicalizeUuid(uuidString);
        }
        catch (IllegalArgumentException e) {
            return null;
        }

        String displayName = hasText(p.getName()) ? p.getName() : username;

        return new UuidDisplayName(uuid, displayName);
    }

    private ProfileSearchResult searchProfiles(int page, ProfileCriteria... criteria) throws IOException, ParseException {
        String body = createBody(criteria);

        URL url = new URL("https://api.mojang.com/profiles/page/" + page);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        
        DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
        try {
            writer.write(body.getBytes(Charsets.UTF_8));
            writer.flush();
        }
        finally {
            writer.close();
        }

        Reader reader = new InputStreamReader(connection.getInputStream());
        JSONObject response;
        try {
            JSONParser parser = new JSONParser();
            response = (JSONObject)parser.parse(reader);
        }
        finally {
            reader.close();
        }
        
        JSONArray profiles = (JSONArray)response.get("profiles");
        Number size = (Number)response.get("size");
        return new ProfileSearchResult(parseProfiles(profiles), size.intValue());
    }

    private List<Profile> parseProfiles(JSONArray profiles) {
        List<Profile> result = new ArrayList<Profile>();
        for (Object obj : profiles) {
            JSONObject jsonProfile = (JSONObject)obj;
            String id = (String)jsonProfile.get("id");
            String name = (String)jsonProfile.get("name");
            result.add(new Profile(id, name));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private String createBody(ProfileCriteria... criteria) {
        List<JSONObject> jsonArray = new ArrayList<JSONObject>();
        for (ProfileCriteria pc : criteria) {
            JSONObject obj = new JSONObject();
            obj.put("name", pc.getName());
            obj.put("agent", pc.getAgent());
            jsonArray.add(obj);
        }
        return JSONValue.toJSONString(jsonArray);
    }

    private static class ProfileCriteria {
        
        private final String name;
        
        private final String agent;

        public ProfileCriteria(String name, String agent) {
            this.name = name;
            this.agent = agent;
        }

        public String getName() {
            return name;
        }

        public String getAgent() {
            return agent;
        }
        
    }

    private static class ProfileSearchResult {

        private final List<Profile> profiles;

        private final int size;

        private ProfileSearchResult(List<Profile> profiles, int size) {
            this.profiles = Collections.unmodifiableList(profiles);
            this.size = size;
        }

        public List<Profile> getProfiles() {
            return profiles;
        }

        public int getSize() {
            return size;
        }

    }

    private static class Profile {
        
        private final String id;
        
        private final String name;

        private Profile(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

    }

}
