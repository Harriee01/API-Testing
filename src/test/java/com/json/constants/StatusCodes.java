package com.json.Constants;

/**
 * StatusCodes – maps well-known HTTP status code integers to semantic names.
 *
 * <p>
 * Using named constants everywhere instead of inline integers (e.g. 200, 404)
 * prevents typos and makes test intent immediately clear to any reader.
 * Shared across all 6 resource test packages.
 *
 * <p>
 * This class is {@code final} with a private constructor: it is a pure
 * constants holder, never instantiated.
 */
public final class StatusCodes {

    // ── 2xx Success ──────────────────────────────────────────────────────────

    /** 200 OK – standard success for GET, PUT, PATCH, DELETE. */
    public static final int OK = 200;

    /** 201 Created – returned by JSONPlaceholder after a successful POST. */
    public static final int CREATED = 201;

    /**
     * 204 No Content – body-less success (not used by JSONPlaceholder but defined
     * for completeness).
     */
    public static final int NO_CONTENT = 204;

    // ── 4xx Client Error ─────────────────────────────────────────────────────

    /** 400 Bad Request – malformed syntax or invalid request. */
    public static final int BAD_REQUEST = 400;

    /** 401 Unauthorized – missing or invalid authentication. */
    public static final int UNAUTHORIZED = 401;

    /** 403 Forbidden – authenticated but not permitted. */
    public static final int FORBIDDEN = 403;

    /**
     * 404 Not Found – the requested resource does not exist.
     * JSONPlaceholder returns this for out-of-range ids (e.g. /posts/9999).
     */
    public static final int NOT_FOUND = 404;

    /** 405 Method Not Allowed – e.g. DELETE on a collection endpoint. */
    public static final int METHOD_NOT_ALLOWED = 405;

    /** 422 Unprocessable Entity – well-formed but semantically invalid request. */
    public static final int UNPROCESSABLE_ENTITY = 422;

    // ── 5xx Server Error ─────────────────────────────────────────────────────

    /** 500 Internal Server Error – unexpected server-side failure. */
    public static final int INTERNAL_SERVER_ERROR = 500;

    // ─────────────────────────────────────────────────────────────────────────

    /** Private constructor – prevents instantiation. */
    private StatusCodes() {
        throw new UnsupportedOperationException("StatusCodes is a constants class and cannot be instantiated.");
    }
}
