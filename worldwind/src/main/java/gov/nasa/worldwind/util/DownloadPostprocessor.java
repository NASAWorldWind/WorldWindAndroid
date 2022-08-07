package gov.nasa.worldwind.util;

/**
 * Interface for resource download post-processing
 */
public interface DownloadPostprocessor<T> {
    /**
     * Process resource according to specified algorithm implementation
     *
     * @param resource original resource
     * @return processed resource
     */
    T process(T resource);
}
