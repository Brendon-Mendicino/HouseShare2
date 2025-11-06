package lol.terabrendon.houseshare2.data.remote.api


import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI
import java.net.URISyntaxException
import java.util.Collections
import java.util.concurrent.locks.ReentrantLock


// Android-changed: App compat changes and bug fixes.
// b/26456024 Add targetSdkVersion based compatibility for domain matching
// b/33034917 Support clearing cookies by adding it with "max-age=0"
// b/25897688 InMemoryCookieStore ignores scheme (http/https) port and path of the cookie
// Remove cookieJar and domainIndex. Use urlIndex as single Cookie storage
// Fix InMemoryCookieStore#remove to verify cookie URI before removal
// Fix InMemoryCookieStore#removeAll to return false if it's empty.
/**
 * A simple in-memory java.net.CookieStore implementation
 *
 * BRENDON COMMENT: this is the biggest piece of garbage I've
 * ever done, I'm so proud of myself :,)
 *
 * @author Edward Wang
 * @since 1.6
 * @hide Visible for testing only.
 */
@Suppress("unchecked_cast")
class SharedPrefCookieStore(
    private val cookieStore: SharedCookieIndexStore,
) : CookieStore {
    // the in-memory representation of cookies
    // BEGIN Android-removed: Remove cookieJar and domainIndex.
    /*
    private List<HttpCookie> cookieJar = null;

    // the cookies are indexed by its domain and associated uri (if present)
    // CAUTION: when a cookie removed from main data structure (i.e. cookieJar),
    //          it won't be cleared in domainIndex & uriIndex. Double-check the
    //          presence of cookie when retrieve one form index store.
    private Map<String, List<HttpCookie>> domainIndex = null;
    */
    // END Android-removed: Remove cookieJar and domainIndex.
    private var uriIndex: MutableMap<URI?, MutableList<HttpCookie?>>? = null

    // use ReentrantLock instead of syncronized for scalability
    private var lock: ReentrantLock? = null

    // BEGIN Android-changed: Add targetSdkVersion and remove cookieJar and domainIndex.
    private val applyMCompatibility: Boolean

    /**
     * The default ctor
     */
    init {
        uriIndex = cookieStore.get().map { (k, v) -> k to v.toMutableList() }
            .toMap(mutableMapOf()) as MutableMap<URI?, MutableList<HttpCookie?>>?
        lock = ReentrantLock(false)
        applyMCompatibility = false
    }

    // END Android-changed: Add targetSdkVersion and remove cookieJar and domainIndex.
    /**
     * Add one cookie into cookie store.
     */
    override fun add(uri: URI?, cookie: HttpCookie?) {
        // pre-condition : argument can't be null
        if (cookie == null) {
            throw NullPointerException("cookie is null")
        }

        lock!!.lock()
        try {
            // Android-changed: Android supports clearing cookies. http://b/33034917
            // They are cleared by adding the cookie with max-age: 0.
            //if (cookie.getMaxAge() != 0) {
            addIndex<URI?>(uriIndex!!, getEffectiveURI(uri), cookie)
            //}

            // store to shared
            cookieStore.set(uriIndex as Map<URI, List<HttpCookie>>)
        } finally {
            lock!!.unlock()
        }
    }


    /**
     * Get all cookies, which:
     * 1) given uri domain-matches with, or, associated with
     * given uri when added to the cookie store.
     * 3) not expired.
     * See RFC 2965 sec. 3.3.4 for more detail.
     */
    override fun get(uri: URI?): MutableList<HttpCookie?> {
        // argument can't be null
        if (uri == null) {
            throw NullPointerException("uri is null")
        }

        val cookies: MutableList<HttpCookie?> = ArrayList<HttpCookie?>()
        // BEGIN Android-changed: InMemoryCookieStore ignores scheme (http/https). b/25897688
        lock!!.lock()
        try {
            // check domainIndex first
            getInternal1(cookies, uriIndex!!, uri.getHost())
            // check uriIndex then
            getInternal2<URI?>(cookies, uriIndex!!, getEffectiveURI(uri))
        } finally {
            lock!!.unlock()
        }
        // END Android-changed: InMemoryCookieStore ignores scheme (http/https). b/25897688
        return cookies
    }

    /**
     * Get all cookies in cookie store, except those have expired
     */
    override fun getCookies(): MutableList<HttpCookie> {
        // BEGIN Android-changed: Remove cookieJar and domainIndex.
        var rt: MutableList<HttpCookie> = ArrayList()

        lock!!.lock()
        try {
            for (list in uriIndex!!.values) {
                val it: MutableIterator<HttpCookie?> = list.iterator()
                while (it.hasNext()) {
                    val cookie = it.next()
                    if (cookie!!.hasExpired()) {
                        it.remove()
                    } else if (!rt.contains(cookie)) {
                        rt.add(cookie)
                    }
                }
            }

            // store to shared
        } finally {
            rt = Collections.unmodifiableList<HttpCookie?>(rt)
            lock!!.unlock()
        }

        // END Android-changed: Remove cookieJar and domainIndex.
        return rt
    }

    /**
     * Get all URIs, which are associated with at least one cookie
     * of this cookie store.
     */
    override fun getURIs(): MutableList<URI?> {
        // BEGIN Android-changed: App compat. Return URI with no cookies. http://b/65538736
        /*
        List<URI> uris = new ArrayList<>();

        lock.lock();
        try {
            Iterator<URI> it = uriIndex.keySet().iterator();
            while (it.hasNext()) {
                URI uri = it.next();
                List<HttpCookie> cookies = uriIndex.get(uri);
                if (cookies == null || cookies.size() == 0) {
                    // no cookies list or an empty list associated with
                    // this uri entry, delete it
                    it.remove();
                }
            }
        } finally {
            uris.addAll(uriIndex.keySet());
            lock.unlock();
        }

        return uris;
         */
        lock!!.lock()
        try {
            val result: MutableList<URI?> = ArrayList<URI?>(uriIndex!!.keys)
            result.remove(null)
            return Collections.unmodifiableList<URI?>(result)
        } finally {
            lock!!.unlock()
        }
        // END Android-changed: App compat. Return URI with no cookies. http://b/65538736
    }


    /**
     * Remove a cookie from store
     */
    override fun remove(uri: URI?, ck: HttpCookie?): Boolean {
        // argument can't be null
        var uri = uri
        if (ck == null) {
            throw NullPointerException("cookie is null")
        }

        // BEGIN Android-changed: Fix uri not being removed from uriIndex.
        lock!!.lock()
        try {
            uri = getEffectiveURI(uri)
            if (uriIndex!!.get(uri) == null) {
                return false
            } else {
                val cookies = uriIndex!!.get(uri)
                if (cookies != null) {
                    return cookies.remove(ck).also {

                        // store to shared
                        cookieStore.set(uriIndex as Map<URI, List<HttpCookie>>)
                    }
                } else {
                    return false
                }
            }

        } finally {
            lock!!.unlock()
        }
        // END Android-changed: Fix uri not being removed from uriIndex.
    }


    /**
     * Remove all cookies in this cookie store.
     */
    override fun removeAll(): Boolean {
        lock!!.lock()
        // BEGIN Android-changed: Let removeAll() return false when there are no cookies.
        var result = false

        try {
            result = !uriIndex!!.isEmpty()
            uriIndex!!.clear()

            // store to shared
            cookieStore.set(uriIndex as Map<URI, List<HttpCookie>>)
        } finally {
            lock!!.unlock()
        }

        return result
        // END Android-changed: Let removeAll() return false when there are no cookies.
    }


    /* ---------------- Private operations -------------- */ /*
     * This is almost the same as HttpCookie.domainMatches except for
     * one difference: It won't reject cookies when the 'H' part of the
     * domain contains a dot ('.').
     * I.E.: RFC 2965 section 3.3.2 says that if host is x.y.domain.com
     * and the cookie domain is .domain.com, then it should be rejected.
     * However that's not how the real world works. Browsers don't reject and
     * some sites, like yahoo.com do actually expect these cookies to be
     * passed along.
     * And should be used for 'old' style cookies (aka Netscape type of cookies)
     */
    private fun netscapeDomainMatches(domain: String?, host: String?): Boolean {
        if (domain == null || host == null) {
            return false
        }

        // if there's no embedded dot in domain and domain is not .local
        val isLocalDomain = ".local".equals(domain, ignoreCase = true)
        var embeddedDotInDomain = domain.indexOf('.')
        if (embeddedDotInDomain == 0) {
            embeddedDotInDomain = domain.indexOf('.', 1)
        }
        if (!isLocalDomain && (embeddedDotInDomain == -1 || embeddedDotInDomain == domain.length - 1)) {
            return false
        }

        // if the host name contains no dot and the domain name is .local
        val firstDotInHost = host.indexOf('.')
        if (firstDotInHost == -1 && isLocalDomain) {
            return true
        }

        val domainLength = domain.length
        val lengthDiff = host.length - domainLength
        if (lengthDiff == 0) {
            // if the host name and the domain name are just string-compare euqal
            return host.equals(domain, ignoreCase = true)
        } else if (lengthDiff > 0) {
            // need to check H & D component
            val D = host.substring(lengthDiff)

            // Android-changed: b/26456024 targetSdkVersion based compatibility for domain matching.
            // Android M and earlier: Cookies with domain "foo.com" would not match "bar.foo.com".
            // The RFC dictates that the user agent must treat those domains as if they had a
            // leading period and must therefore match "bar.foo.com".
            if (applyMCompatibility && !domain.startsWith(".")) {
                return false
            }

            return (D.equals(domain, ignoreCase = true))
        } else if (lengthDiff == -1) {
            // if domain is actually .host
            return (domain.get(0) == '.' &&
                    host.equals(domain.substring(1), ignoreCase = true))
        }

        return false
    }

    private fun getInternal1(
        cookies: MutableList<HttpCookie?>, cookieIndex: MutableMap<URI?, MutableList<HttpCookie?>>,
        host: String?
    ) {
        // BEGIN Android-changed: InMemoryCookieStore ignores scheme (http/https). b/25897688
        // Use a separate list to handle cookies that need to be removed so
        // that there is no conflict with iterators.
        val toRemove = ArrayList<HttpCookie?>()
        for (entry in cookieIndex.entries) {
            val lst: MutableList<HttpCookie?> = entry.value
            for (c in lst) {
                val domain = c!!.getDomain()
                if ((c.getVersion() == 0 && netscapeDomainMatches(domain, host)) ||
                    (c.getVersion() == 1 && HttpCookie.domainMatches(domain, host))
                ) {
                    // the cookie still in main cookie store

                    if (!c.hasExpired()) {
                        // don't add twice
                        if (!cookies.contains(c)) {
                            cookies.add(c)
                        }
                    } else {
                        toRemove.add(c)
                    }
                }
            }
            // Clear up the cookies that need to be removed
            for (c in toRemove) {
                lst.remove(c)
            }
            toRemove.clear()
        }
        // END Android-changed: InMemoryCookieStore ignores scheme (http/https). b/25897688
    }

    // @param cookies           [OUT] contains the found cookies
    // @param cookieIndex       the index
    // @param comparator        the prediction to decide whether or not
    //                          a cookie in index should be returned
    private fun <T : Comparable<T?>?>
            getInternal2(
        cookies: MutableList<HttpCookie?>, cookieIndex: MutableMap<T?, MutableList<HttpCookie?>>,
        comparator: T?
    ) {
        // BEGIN Android-changed: InMemoryCookieStore ignores scheme (http/https). b/25897688
        // Removed cookieJar
        for (index in cookieIndex.keys) {
            if ((index === comparator) || (index != null && comparator!!.compareTo(index) == 0)) {
                val indexedCookies = cookieIndex.get(index)
                // check the list of cookies associated with this domain
                if (indexedCookies != null) {
                    val it: MutableIterator<HttpCookie?> = indexedCookies.iterator()
                    while (it.hasNext()) {
                        val ck = it.next()
                        // the cookie still in main cookie store
                        if (!ck!!.hasExpired()) {
                            // don't add twice
                            if (!cookies.contains(ck)) cookies.add(ck)
                        } else {
                            it.remove()
                        }
                    }
                } // end of indexedCookies != null
            } // end of comparator.compareTo(index) == 0
        } // end of cookieIndex iteration

        // END Android-changed: InMemoryCookieStore ignores scheme (http/https). b/25897688
    }

    // add 'cookie' indexed by 'index' into 'indexStore'
    private fun <T> addIndex(
        indexStore: MutableMap<T?, MutableList<HttpCookie?>>,
        index: T?,
        cookie: HttpCookie?
    ) {
        // Android-changed: "index" can be null.
        // We only use the URI based index on Android and we want to support null URIs. The
        // underlying store is a HashMap which will support null keys anyway.
        // if (index != null) {
        var cookies = indexStore.get(index)
        if (cookies != null) {
            // there may already have the same cookie, so remove it first
            cookies.remove(cookie)

            cookies.add(cookie)
        } else {
            cookies = ArrayList<HttpCookie?>()
            cookies.add(cookie)
            indexStore.put(index, cookies)
        }
    }


    //
    // for cookie purpose, the effective uri should only be http://host
    // the path will be taken into account when path-match algorithm applied
    //
    private fun getEffectiveURI(uri: URI?): URI? {
        var effectiveURI: URI? = null
        // Android-added: Fix NullPointerException.
        if (uri == null) {
            return null
        }
        try {
            effectiveURI = URI(
                "http",
                uri.getHost(),
                null,  // path component
                null,  // query component
                null // fragment component
            )
        } catch (ignored: URISyntaxException) {
            effectiveURI = uri
        }

        return effectiveURI
    }
}
