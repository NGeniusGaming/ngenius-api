package com.ngenenius.api.service

/**
 * The Twitch API has a unique take on pagination, and therefore this should
 * be dealt with in a common approach.
 *
 * To paginate results, twitch can optionally return a 'cursor' reference, which must
 * be sent back to them in the 'after' query parameter in order to receive the next page of results.
 */
abstract class AbstractTwitchPaginatedService {
}
