package com.steven.server.service

import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.exception.AddressNotFoundException
import com.steven.server.model.mongo.ModelManagePO
import com.steven.server.service.mongo.ModelManageService
import com.steven.server.util.Extensions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*


/**
 * check IP address's geo details
 */
@Service
class RegionRestrictService(private val modelManageService: ModelManageService) {
    @Value("\${threshold_of_ip_visit_total}")
    lateinit var thresholdOfIpVisitTotal: String

    @Value("\${threshold_of_ip_visit_daily}")
    lateinit var thresholdOfIpVisitDaily: String

    @Value("#{'\${listOfForbiddenCountries}'.split(',')}")
    lateinit var listOfForbiddenCountries: List<String>


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

            countryName.takeIf { listOfForbiddenCountries.contains(it) }
                ?.run {
                    isInBlackListCountries = true
                    logger.info("[isShallNotPass] ban ip $ip for under black list: $countryName !")
                }

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

        // TODO refactor this mass
        shallNotPass = verifyVisitRecord(ip)
        shallNotPass = isInBlackListCountries
        shallNotPass = !isIpNotInDb // just let them go, if we can't recognize this IP

        return shallNotPass
    }

    /**
     *  update visit record and verify should ban those visit too frequently or not
     *
     *  @param ip ip address
     *  @return [Boolean] true if this IP visit too frequently
     */
    fun verifyVisitRecord(ip: String): Boolean {

        println("listOfIntegers: $listOfForbiddenCountries")

        val todayStr = Extensions().getTodayStr()

        // check record exists or not
        val updatedPO = modelManageService.findByIp(ip)
            .takeIf { it.isPresent }
            ?.run {
                modelManageService.updateCount(this.get(), todayStr)
            } ?: run {
            // create new one if this ip have no record yet
            modelManageService.save(
                ModelManagePO().apply {
                    this.ip = ip
                    this.dateCounts = mutableMapOf(todayStr to 1)
                    this.countTotal = 1
                    this.createTime = Date()
                    this.updateTime = Date()
                }
            )

            return true
        }

        // check ip record is visit too frequently, return true if visit too frequently
        return when {
            updatedPO.isCountTotalOverThreshold() -> false
            updatedPO.isDateCountOverThreshold(todayStr) -> false

            else -> true
        }
    }

    private fun ModelManagePO.isDateCountOverThreshold(todayStr: String): Boolean {
        val toDayCount = this.dateCounts[todayStr]
        requireNotNull(toDayCount) { return true }
        return toDayCount > thresholdOfIpVisitTotal.toInt()
    }

    private fun ModelManagePO.isCountTotalOverThreshold(): Boolean {
        return this.countTotal > thresholdOfIpVisitDaily.toInt()
    }
}
