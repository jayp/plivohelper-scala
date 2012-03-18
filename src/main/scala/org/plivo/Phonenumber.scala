package org.plivo

import java.io.Serializable
import org.slf4j.{ LoggerFactory, Logger }

/**
 * Immutable representation of a phonenumber. Will ALWAYS contain international
 * prefix and support outputting to the various formats used in the system.
 */
case class Phonenumber(countryCode: String,
                       number: String) {

  /**
   * Name of the country to which the phone number belongs to
   */
  def countryName =
    Phonenumber.getCountryName(countryCode)

  /**
   * The number on the E.164 format (+PPNNNNNNNN), e.g. +4790055338.
   */
  def toStandardFormat =
    "+" + countryCode + number

  def toClassicFormat =
    countryCode + number

  override def toString =
    toStandardFormat
}

object Phonenumber {

  def apply(number: String): Phonenumber =
    apply(number, true)

  def apply(number: String,
            countryCodeIncluded: Boolean): Phonenumber =
    parse(number, countryCodeIncluded)

  def apply(number: Option[String]): Option[Phonenumber] =
    apply(number, true)

  def apply(number: Option[String],
            countryCodeIncluded: Boolean): Option[Phonenumber] =
    number match {
      case Some(num) => Some(parse(num, countryCodeIncluded))
      case None      => None
    }

  implicit def string2Phonenumber(number: String): Phonenumber =
    Phonenumber.apply(number)

  private val log = LoggerFactory.getLogger(getClass)
  private val defaultCountryCode = "1"

  /**
   * Parse a string into a phonenumber. If the string passed does not contain
   * country code, the {@link #DEFAULT_COUNTRY_CODE} will be used.
   */
  private def parse(number: String,
                    countryCodeIncluded: Boolean): Phonenumber = {
    if (number == null)
      throw new IllegalArgumentException("number cannot be null")
    if (!number.matches("[\\+]?[\\d]+"))
      throw new IllegalArgumentException("number must contain digits and an " +
        "optional international prefix only. (" + number + ")")

    val trimmed = number.replaceAll("\\s", "")
    if (trimmed.length() == 0)
      throw new IllegalArgumentException("trimmed number is empty")

    makePhonenumber(trimmed, countryCodeIncluded)
  }

  /**
   * Make a phonenumber object from a string number
   */
  private def makePhonenumber(number: String,
                              countryCodeIncluded: Boolean): Phonenumber =
    if (number.startsWith("+"))
      determineCountryCode(number.substring(1))
    else if (number.startsWith("00"))
      determineCountryCode(number.substring(2))
    else if (countryCodeIncluded)
      determineCountryCode(number)
    else
      new Phonenumber(defaultCountryCode, number)

  /**
   * Break down the number into it's country code, and the country-specific
   * number.
   */
  private def determineCountryCode(numberWithCC: String): Phonenumber = {
      // A country code has 1-3 digits and, fortunately, the series are structured
      // so that there is no overlap between phone number with a shorter country
      // code and phone number with a longer country code.
      def checkCCLen(ccLen: Int): Phonenumber = {
        if (ccLen == 0) {
          log.warn("Could not find country code for " + numberWithCC +
            " in country code table! Returning default.")
          new Phonenumber(defaultCountryCode, numberWithCC)
        } else {
          val candidate = numberWithCC.substring(0, ccLen)
          if (countryCodes.contains(candidate))
            new Phonenumber(candidate, numberWithCC.substring(candidate.length()))
          else
            checkCCLen(ccLen - 1)
        }
      }

    checkCCLen(3)
  }

  /**
   * Known telephone prefix codes for countries
   */
  private val countryCodes = Map(
    "355" -> "Albania",
    "213" -> "Algeria",
    "376" -> "Andorra",
    "244" -> "Angola",
    "264" -> "Anguilla",
    "268" -> "Antigua and Barbuda",
    "54" -> "Argentina",
    "374" -> "Armenia",
    "297" -> "Aruba",
    "247" -> "Ascension Island",
    "61" -> "Australia",
    "43" -> "Austria",
    "994" -> "Azerbaijan",
    "242" -> "Bahamas",
    "973" -> "Bahrain",
    "880" -> "Bangladesh",
    "246" -> "Barbados",
    "375" -> "Belarus",
    "32" -> "Belgium",
    "501" -> "Belize",
    "229" -> "Benin",
    "441" -> "Bermuda",
    "975" -> "Bhutan",
    "591" -> "Bolivia",
    "387" -> "Bosnia",
    "267" -> "Botswana",
    "55" -> "Brazil",
    "673" -> "Brunei",
    "359" -> "Bulgaria",
    "226" -> "Burkina Faso",
    "257" -> "Burundi",
    "855" -> "Cambodia",
    "237" -> "Cameroon",
    "1" -> "Canada",
    "238" -> "Cape Verde Islands",
    "345" -> "Cayman Islands",
    "236" -> "Central Africa Republic",
    "235" -> "Chad",
    "56" -> "Chile",
    "86" -> "China",
    "57" -> "Columbia",
    "269" -> "Comoros Island",
    "242" -> "Congo",
    "682" -> "Cook Islands",
    "506" -> "Costa Rica",
    "385" -> "Croatia",
    "53" -> "Cuba",
    "357" -> "Cyprus",
    "420" -> "Czech Republic",
    "243" -> "Democratic Republic of Congo (Zaire)",
    "45" -> "Denmark",
    "246" -> "Diego Garcia",
    "253" -> "Djibouti",
    "767" -> "Dominica Islands",
    "809" -> "Dominican Republic",
    "593" -> "Ecuador",
    "20" -> "Egypt",
    "503" -> "El Salvador",
    "240" -> "Equatorial Guinea",
    "291" -> "Eritrea",
    "372" -> "Estonia",
    "251" -> "Ethiopia",
    "298" -> "Faeroe Islands",
    "500" -> "Falkland Islands",
    "679" -> "Fiji Islands",
    "358" -> "Finland",
    "33" -> "France",
    "594" -> "French Guiana?",
    "689" -> "French Polynesia",
    "241" -> "Gabon",
    "995" -> "Georgia",
    "49" -> "Germany",
    "233" -> "Ghana",
    "350" -> "Gibraltar",
    "30" -> "Greece",
    "299" -> "Greenland",
    "473" -> "Grenada",
    "590" -> "Guadeloupe",
    "671" -> "Guam",
    "502" -> "Guatemala",
    "245" -> "Guinea Bissau",
    "224" -> "Guinea Republic",
    "592" -> "Guyana",
    "509" -> "Haiti",
    "503" -> "Honduras",
    "852" -> "Hong Kong",
    "36" -> "Hungary",
    "354" -> "Iceland",
    "91" -> "India",
    "62" -> "Indonesia",
    "98" -> "Iran",
    "964" -> "Iraq",
    "353" -> "Ireland",
    "972" -> "Israel",
    "39" -> "Italy",
    "225" -> "Ivory Coast",
    "876" -> "Jamaica",
    "81" -> "Japan",
    "962" -> "Jordan",
    "7" -> "Kazakhstan",
    "254" -> "Kenya",
    "686" -> "Kiribati",
    "850" -> "Korea, North",
    "82" -> "Korea, South",
    "965" -> "Kuwait",
    "996" -> "Kyrgyzstan",
    "856" -> "Laos",
    "371" -> "latvia",
    "961" -> "Lebanon",
    "266" -> "Lesotho",
    "231" -> "Liberia",
    "218" -> "Libya",
    "423" -> "Liechtenstein",
    "370" -> "Lithuania",
    "352" -> "Luxembourg",
    "853" -> "Macau",
    "389" -> "Macedonia (Fyrom)",
    "261" -> "Madagascar",
    "265" -> "Malawi",
    "60" -> "Malaysia",
    "960" -> "Maldives Republic",
    "223" -> "Mali",
    "356" -> "Malta",
    "670" -> "Mariana Islands",
    "692" -> "Marshall Islands",
    "596" -> "Martinique",
    "230" -> "Mauritius",
    "269" -> "Mayotte Islands",
    "52" -> "Mexico",
    "691" -> "Micronesia",
    "373" -> "Moldova",
    "377" -> "Monaco",
    "976" -> "Mongolia",
    "664" -> "Montserrat",
    "212" -> "Morocco",
    "258" -> "Mozambique",
    "95" -> "Myanmar (Burma)",
    "264" -> "Namibia",
    "674" -> "Nauru",
    "977" -> "Nepal",
    "31" -> "Netherlands",
    "599" -> "Netherlands Antilles",
    "687" -> "New Caledonia",
    "64" -> "New Zealand",
    "505" -> "Nicaragua",
    "227" -> "Niger",
    "234" -> "Nigeria",
    "683" -> "Niue Island",
    "672" -> "Norfolk Island",
    "47" -> "Norway",
    "968" -> "Oman",
    "92" -> "Pakistan",
    "680" -> "Palau",
    "970" -> "Palestine",
    "507" -> "Panama",
    "675" -> "Papua New Guinea",
    "595" -> "Paraguay",
    "51" -> "Peru",
    "63" -> "Philippines",
    "48" -> "Poland",
    "351" -> "Portugal",
    "787" -> "Puerto Rico",
    "974" -> "Qatar",
    "262" -> "Reunion Island",
    "40" -> "Romania",
    "7" -> "Russia",
    "250" -> "Rwanda",
    "684" -> "Samoa (American)",
    "685" -> "Samoa (Western)",
    "378" -> "San Marino",
    "239" -> "Sao Tome & Principe",
    "966" -> "Saudi Arabia",
    "221" -> "Senegal",
    "381" -> "Serbia",
    "248" -> "Seychelles",
    "232" -> "Sierra Leone",
    "65" -> "Singapore",
    "421" -> "Slovak Republic",
    "386" -> "Slovenia",
    "677" -> "Solomon Islands",
    "252" -> "Somalia",
    "27" -> "South Africa",
    "34" -> "Spain",
    "94" -> "Sri Lanka",
    "290" -> "St Helena",
    "869" -> "St Kitts & Nevia",
    "758" -> "St Lucia",
    "249" -> "Sudan",
    "597" -> "Surinam",
    "268" -> "Swaziland",
    "46" -> "Sweden",
    "41" -> "Switzerland",
    "963" -> "Syria",
    "886" -> "Taiwan",
    "992" -> "Tajikistan",
    "255" -> "Tanzania",
    "66" -> "Thailand",
    "220" -> "The Gambia",
    "228" -> "Togo",
    "676" -> "Tonga",
    "868" -> "Trinidad & Tobago",
    "216" -> "Tunisia",
    "90" -> "Turkey",
    "993" -> "Turkmenistan",
    "649" -> "Turks & Caicos Islands",
    "688" -> "Tuvalu",
    "256" -> "Uganda",
    "380" -> "Ukraine",
    "971" -> "United Arab Emirates",
    "44" -> "United Kingdom",
    "598" -> "Uruguay",
    "1" -> "USA",
    "998" -> "Uzbekistan",
    "678" -> "Vanuatu",
    "58" -> "Venezuela",
    "84" -> "Vietnam",
    "681" -> "Wallis & Futuna Islands",
    "967" -> "Yemen Arab Republic",
    "260" -> "Zambia",
    "263" -> "Zimbabwe")

  /**
   * Determine the country name given a country code
   */
  def getCountryName(countryCode: String): Option[String] =
    countryCodes.get(countryCode)
}