/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class LruMemoryCache<K, V> {

    protected final HashMap<K, Entry<K, V>> entries = new HashMap<>();

    protected final Comparator<Entry<K, V>> lruComparator = new Comparator<Entry<K, V>>() {
        @Override
        public int compare(Entry<K, V> lhs, Entry<K, V> rhs) {
            return (int) (lhs.lastUsed - rhs.lastUsed); // sorts entries from least recently used to most recently used
        }
    };

    protected int capacity;

    protected int lowWater;

    protected int usedCapacity;

    public LruMemoryCache(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LruMemoryCache", "constructor", "invalidCapacity"));
        }

        this.capacity = capacity;
        this.lowWater = (int) (capacity * 0.75);
    }

    public LruMemoryCache(int capacity, int lowWater) {
        if (capacity < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LruMemoryCache", "constructor", "invalidCapacity"));
        }

        if (lowWater >= capacity || lowWater < 0) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "LruMemoryCache", "constructor",
                "The specified low-water value is greater than or equal to the capacity, or less than 1"));
        }

        this.capacity = capacity;
        this.lowWater = lowWater;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public int getUsedCapacity() {
        return this.usedCapacity;
    }

    public int getEntryCount() {
        return this.entries.size();
    }

    public V get(K key) {
        Entry<K, V> entry = this.entries.get(key);
        if (entry != null) {
            entry.lastUsed = System.currentTimeMillis();
            return entry.value;
        } else {
            return null;
        }
    }

    public V put(K key, V value, int size) {
        // TODO can we reuse the same entry when only the size has changed?
        // TODO use case is high frequency texture updates, where size may or may not change
        Entry<K, V> newEntry = new Entry<>(key, value, size);
        Entry<K, V> oldEntry = this.putEntry(newEntry);
        return (oldEntry != null) ? oldEntry.value : null;
    }

    public V remove(K key) {
        Entry<K, V> entry = this.entries.remove(key);
        if (entry != null) {
            this.usedCapacity -= entry.size;
            this.entryRemoved(entry);
            return entry.value;
        } else {
            return null;
        }
    }

    public boolean containsKey(K key) {
        return this.entries.containsKey(key);
    }

    public void clear() {
        for (Entry<K, V> entry : this.entries.values()) {
            this.entryRemoved(entry);
        }

        this.entries.clear();
        this.usedCapacity = 0;
    }

    protected void makeSpace(int spaceRequired) {
        // Sort the entries from least recently used to most recently used, then remove the least recently used entries
        // until the cache capacity reaches the low water and the cache has enough free capacity for the required space.

        ArrayList<Entry<K, V>> sortedEntries = new ArrayList<>(this.entries.size());

        for (Entry<K, V> entry : this.entries.values()) {
            sortedEntries.add(entry);
        }

        Collections.sort(sortedEntries, this.lruComparator);

        for (Entry<K, V> entry : sortedEntries) {
            if (this.usedCapacity > this.lowWater || (this.capacity - this.usedCapacity) < spaceRequired) {
                this.entries.remove(entry.key);
                this.usedCapacity -= entry.size;
                this.entryRemoved(entry);
            } else {
                break;
            }
        }
    }

    protected Entry<K, V> putEntry(Entry<K, V> newEntry) {
        if (this.usedCapacity + newEntry.size > this.capacity) {
            this.makeSpace(newEntry.size);
        }

        newEntry.lastUsed = System.currentTimeMillis();
        this.usedCapacity += newEntry.size;

        Entry<K, V> oldEntry = this.entries.put(newEntry.key, newEntry);
        if (oldEntry != null) {
            this.usedCapacity -= oldEntry.size;

            if (newEntry.value != oldEntry.value) {
                this.entryReplaced(oldEntry, newEntry);
                return oldEntry;
            }
        }

        return null;
    }

    protected void entryRemoved(Entry<K, V> entry) {
    }

    protected void entryReplaced(Entry<K, V> oldEntry, Entry<K, V> newEntry) {

    }

    protected static class Entry<K, V> {

        public final K key;

        public final V value;

        public final int size;

        public long lastUsed;

        public Entry(K key, V value, int size) {
            this.key = key;
            this.value = value;
            this.size = size;
        }
    }
}
