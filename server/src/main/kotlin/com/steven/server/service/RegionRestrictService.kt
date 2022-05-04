package com.steven.server.service

import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.exception.AddressNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.net.InetAddress
import java.net.UnknownHostException


/**
 * check IP address's geo details
 */
@Service
class RegionRestrictService {
    private val logger = LoggerFactory.getLogger(RegionRestrictService::class.java)

    val dbReader: DatabaseReader =
        DatabaseReader.Builder(File(javaClass.classLoader.getResource("GeoLite2-City.mmdb")!!.path)).build()

    /**
     * if client in black list region or visit too frequently, they should not pass.
     *
     * NOTE: those not found in our DB, just let them pass
     *
     * @see 'https://dev.maxmind.com/geoip/geolite2-free-geolocation-data'
     *
     * @return [Boolean] true: client shall not pass, false: just let them go
     */
    fun isShallNotPass(ip: String): Boolean {

        val blackListCountries = mutableListOf("Russia") // add restrict regions what you want to restrict to list

        var isInBlackListCountries = false
        var isIpNotInDb = false
        var shallNotPass = false

        try {
            val response = dbReader.city(InetAddress.getByName(ip))
            val countryName = response.country.name
            val cityName = response.city.name
            val postal = response.postal.code
            val state = response.leastSpecificSubdivision.name

            logger.info("[isShallNotPass] clientIP: $ip, countryName: $countryName, cityName: $cityName, postal: $postal, state: $state")

            countryName.takeIf { blackListCountries.contains(it) }
                ?.run { isInBlackListCountries = true }


        } catch (e: UnknownHostException) {
            e.printStackTrace()
            logger.error("[isShallNotPass] go to find your mama, dude!")
            return true
        } catch (e: AddressNotFoundException) {
            logger.warn("[isShallNotPass] address: $ip not found in data base!")
            isIpNotInDb = true
        } catch (e: Exception) {
            e.printStackTrace()
            isIpNotInDb = false
        }

        if (isIpNotInDb) {
            logger.warn("[isShallNotPass] need to count this IP access! : $ip")
            // TODO should count the visit record and ban those visit too frequently
        }

        shallNotPass = isInBlackListCountries
        shallNotPass = !isIpNotInDb // just let them go, if we can't recognize this IP

        return shallNotPass
    }
}
