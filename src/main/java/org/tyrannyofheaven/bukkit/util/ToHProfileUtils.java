/*
 * Copyright 2012 ZerothAngel <zerothangel@tyrannyofheaven.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tyrannyofheaven.bukkit.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Simple & naive profiling, for development. Don't use in production code!
 * 
 * @author zerothangel
 */
public class ToHProfileUtils {

    private static final Logger logger = Logger.getLogger(ToHProfileUtils.class.getName());

    private static ConcurrentMap<String, ProfileData> profileData = new ConcurrentHashMap<String, ProfileData>();

    private static ProfileData getProfileData(String name) {
        // Avoid cost of initial creation
        ProfileData pd = profileData.get(name);
        if (pd == null) {
            pd = new ProfileData();
            ProfileData old = profileData.putIfAbsent(name, pd);
            if (old != null)
                pd = old; // use existing
        }
        return pd;
    }

    public static void profileStart(String name) {
        ProfileData pd = getProfileData(name);
        pd.startTime.set(System.nanoTime());
    }

    public static void profileStop(String name) {
        long end = System.nanoTime();
        ProfileData pd = getProfileData(name);
        long current = end - pd.startTime.get();
        pd.accumulatedTime.addAndGet(current);
        pd.hits.incrementAndGet();
        pd.lastTime.set(current);

        long minTime = pd.minTime.get();
        while (current < minTime) {
            if (pd.minTime.compareAndSet(minTime, current)) break;
            minTime = pd.minTime.get();
        }
        
        long maxTime = pd.maxTime.get();
        while (current > maxTime) {
            if (pd.maxTime.compareAndSet(maxTime, current)) break;
            maxTime = pd.maxTime.get();
        }
    }

    public static void profileReport() {
        for (Map.Entry<String, ProfileData> me : profileData.entrySet()) {
            logger.log(Level.INFO, String.format("%s - %s", me.getKey(), me.getValue()));
        }
    }

    public static void profileReset() {
        profileData.clear();
    }

    public static void profileReset(String name) {
        profileData.remove(name);
    }

    public static void schedulePeriodicReport(Plugin plugin, long interval) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                profileReport();
            }
        }, interval, interval);
    }

    private static class ProfileData {
        
        private final AtomicLong startTime = new AtomicLong();

        private final AtomicLong accumulatedTime = new AtomicLong();
        
        private final AtomicInteger hits = new AtomicInteger();

        private final AtomicLong lastTime = new AtomicLong();

        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        
        private final AtomicLong maxTime = new AtomicLong();

        @Override
        public String toString() {
            long total = accumulatedTime.get();
            int hits = this.hits.get();
            return String.format("total: %d, hits: %d, average: %d (min: %d, max: %d, last: %d)",
                    total, hits, total / hits, minTime.get(), maxTime.get(), lastTime.get());
        }

    }

}
