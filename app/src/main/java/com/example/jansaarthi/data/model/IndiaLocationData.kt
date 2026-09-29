package com.example.jansaarthi.data.model

/**
 * Authentic geographic data for India's 28 States and 8 Union Territories,
 * along with standardized options for citizen profile registration.
 */
object IndiaLocationData {

    val states = listOf(
        "Andhra Pradesh",
        "Arunachal Pradesh",
        "Assam",
        "Bihar",
        "Chhattisgarh",
        "Goa",
        "Gujarat",
        "Haryana",
        "Himachal Pradesh",
        "Jharkhand",
        "Karnataka",
        "Kerala",
        "Madhya Pradesh",
        "Maharashtra",
        "Manipur",
        "Meghalaya",
        "Mizoram",
        "Nagaland",
        "Odisha",
        "Punjab",
        "Rajasthan",
        "Sikkim",
        "Tamil Nadu",
        "Telangana",
        "Tripura",
        "Uttar Pradesh",
        "Uttarakhand",
        "West Bengal",
        "Andaman and Nicobar Islands",
        "Chandigarh",
        "Dadra and Nagar Haveli and Daman and Diu",
        "Delhi",
        "Jammu and Kashmir",
        "Ladakh",
        "Lakshadweep",
        "Puducherry"
    )

    val districtsByState: Map<String, List<String>> = mapOf(
        "Andhra Pradesh" to listOf(
            "Alluri Sitharama Raju", "Anakapalli", "Ananthapuramu", "Annamayya", "Bapatla",
            "Chittoor", "Dr. B.R. Ambedkar Konaseema", "East Godavari", "Eluru", "Guntur",
            "Kakinada", "Krishna", "Kurnool", "Nandyal", "NTR", "Palnadu",
            "Parvathipuram Manyam", "Prakasam", "Sri Potti Sriramulu Nellore", "Sri Sathya Sai",
            "Srikakulam", "Tirupati", "Visakhapatnam", "Vizianagaram", "West Godavari", "YSR Kadapa"
        ),
        "Arunachal Pradesh" to listOf(
            "Anjaw", "Changlang", "Dibang Valley", "East Kameng", "East Siang",
            "Kamle", "Kra Daadi", "Kurung Kumey", "Lepa Rada", "Lohit",
            "Longding", "Lower Dibang Valley", "Lower Siang", "Lower Subansiri", "Namsai",
            "Pakke Kessang", "Papum Pare", "Shi Yomi", "Siang", "Tawang",
            "Tirap", "Upper Siang", "Upper Subansiri", "West Kameng", "West Siang"
        ),
        "Assam" to listOf(
            "Baksa", "Barpeta", "Biswanath", "Bongaigaon", "Cachar",
            "Charaideo", "Chirang", "Darrang", "Dhemaji", "Dhubri",
            "Dibrugarh", "Dima Hasao", "Goalpara", "Golaghat", "Hailakandi",
            "Hojai", "Jorhat", "Kamrup", "Kamrup Metropolitan", "Karbi Anglong",
            "Karimganj", "Kokrajhar", "Lakhimpur", "Majuli", "Morigaon",
            "Nagaon", "Nalbari", "Sivasagar", "Sonitpur", "South Salmara-Mankachar",
            "Tinsukia", "Udalguri", "West Karbi Anglong"
        ),
        "Bihar" to listOf(
            "Araria", "Arwal", "Aurangabad", "Banka", "Begusarai",
            "Bhagalpur", "Bhojpur", "Buxar", "Darbhanga", "East Champaran",
            "Gaya", "Gopalganj", "Jamui", "Jehanabad", "Kaimur",
            "Katihar", "Khagaria", "Kishanganj", "Lakhisarai", "Madhepura",
            "Madhubani", "Munger", "Muzaffarpur", "Nalanda", "Nawada",
            "Patna", "Purnia", "Rohtas", "Saharsa", "Samastipur",
            "Saran", "Sheikhpura", "Sheohar", "Sitamarhi", "Siwan",
            "Supaul", "Vaishali", "West Champaran"
        ),
        "Chhattisgarh" to listOf(
            "Balod", "Baloda Bazar", "Balrampur", "Bastar", "Bemetara",
            "Bijapur", "Bilaspur", "Dantewada", "Dhamtari", "Durg",
            "Gariaband", "Gaurela-Pendra-Marwahi", "Janjgir-Champa", "Jashpur", "Kabirdham",
            "Kanker", "Khairagarh", "Kondagaon", "Korba", "Koriya",
            "Mahasamund", "Manendragarh", "Mohla-Manpur", "Mungeli", "Narayanpur",
            "Raigarh", "Raipur", "Rajnandgaon", "Sarangarh", "Sakti",
            "Sukma", "Surajpur", "Surguja"
        ),
        "Goa" to listOf(
            "North Goa", "South Goa"
        ),
        "Gujarat" to listOf(
            "Ahmedabad", "Amreli", "Anand", "Aravalli", "Banaskantha",
            "Bharuch", "Bhavnagar", "Botad", "Chhota Udaipur", "Dahod",
            "Dang", "Devbhoomi Dwarka", "Gandhinagar", "Gir Somnath", "Jamnagar",
            "Junagadh", "Kheda", "Kutch", "Mahisagar", "Mehsana",
            "Morbi", "Narmada", "Navsari", "Panchmahal", "Patan",
            "Porbandar", "Rajkot", "Sabarkantha", "Surat", "Surendranagar",
            "Tapi", "Vadodara", "Valsad"
        ),
        "Haryana" to listOf(
            "Ambala", "Bhiwani", "Charkhi Dadri", "Faridabad", "Fatehabad",
            "Gurugram", "Hisar", "Jhajjar", "Jind", "Kaithal",
            "Karnal", "Kurukshetra", "Mahendragarh", "Nuh", "Palwal",
            "Panchkula", "Panipat", "Rewari", "Rohtak", "Sirsa",
            "Sonipat", "Yamunanagar"
        ),
        "Himachal Pradesh" to listOf(
            "Bilaspur", "Chamba", "Hamirpur", "Kangra", "Kinnaur",
            "Kullu", "Lahaul and Spiti", "Mandi", "Shimla", "Sirmaur",
            "Solan", "Una"
        ),
        "Jharkhand" to listOf(
            "Bokaro", "Chatra", "Deoghar", "Dhanbad", "Dumka",
            "East Singhbhum", "Garhwa", "Giridih", "Godda", "Gumla",
            "Hazaribagh", "Jamtara", "Khunti", "Koderma", "Latehar",
            "Lohardaga", "Pakur", "Palamu", "Ramgarh", "Ranchi",
            "Sahibganj", "Saraikela Kharsawan", "Simdega", "West Singhbhum"
        ),
        "Karnataka" to listOf(
            "Bagalkot", "Ballari", "Belagavi", "Bengaluru Rural", "Bengaluru Urban",
            "Bidar", "Chamarajanagar", "Chikkaballapura", "Chikkamagaluru", "Chitradurga",
            "Dakshina Kannada", "Davanagere", "Dharwad", "Gadag", "Hassan",
            "Haveri", "Kalaburagi", "Kodagu", "Kolar", "Koppal",
            "Mandya", "Mysuru", "Raichur", "Ramanagara", "Shivamogga",
            "Tumakuru", "Udupi", "Uttara Kannada", "Vijayanagara", "Vijayapura", "Yadgir"
        ),
        "Kerala" to listOf(
            "Alappuzha", "Ernakulam", "Idukki", "Kannur", "Kasaragod",
            "Kollam", "Kottayam", "Kozhikode", "Malappuram", "Palakkad",
            "Pathanamthitta", "Thiruvananthapuram", "Thrissur", "Wayanad"
        ),
        "Madhya Pradesh" to listOf(
            "Agar Malwa", "Alirajpur", "Anuppur", "Ashoknagar", "Balaghat",
            "Barwani", "Betul", "Bhind", "Bhopal", "Burhanpur",
            "Chhatarpur", "Chhindwara", "Damoh", "Datia", "Dewas",
            "Dhar", "Dindori", "Guna", "Gwalior", "Harda",
            "Hoshangabad", "Indore", "Jabalpur", "Jhabua", "Katni",
            "Khandwa", "Khargone", "Mandla", "Mandsaur", "Morena",
            "Narsinghpur", "Neemuch", "Niwari", "Panna", "Raisen",
            "Rajgarh", "Ratlam", "Rewa", "Sagar", "Satna",
            "Sehore", "Seoni", "Shahdol", "Shajapur", "Sheopur",
            "Shivpuri", "Sidhi", "Singrauli", "Tikamgarh", "Ujjain",
            "Umaria", "Vidisha"
        ),
        "Maharashtra" to listOf(
            "Ahmednagar", "Akola", "Amravati", "Chhatrapati Sambhaji Nagar", "Beed",
            "Bhandara", "Buldhana", "Chandrapur", "Dhule", "Gadchiroli",
            "Gondia", "Hingoli", "Jalgaon", "Jalna", "Kolhapur",
            "Latur", "Mumbai City", "Mumbai Suburban", "Nagpur", "Nanded",
            "Nandurbar", "Nashik", "Dharashiv", "Palghar", "Parbhani",
            "Pune", "Raigad", "Ratnagiri", "Sangli", "Satara",
            "Sindhudurg", "Solapur", "Thane", "Wardha", "Washim", "Yavatmal"
        ),
        "Manipur" to listOf(
            "Bishnupur", "Chandel", "Churachandpur", "Imphal East", "Imphal West",
            "Jiribam", "Kakching", "Kamjong", "Kangpokpi", "Noney",
            "Pherzawl", "Senapati", "Tamenglong", "Tengnoupal", "Thoubal", "Ukhrul"
        ),
        "Meghalaya" to listOf(
            "East Garo Hills", "East Jaintia Hills", "East Khasi Hills", "Eastern West Khasi Hills",
            "North Garo Hills", "Ri Bhoi", "South Garo Hills", "South West Garo Hills",
            "South West Khasi Hills", "West Garo Hills", "West Jaintia Hills", "West Khasi Hills"
        ),
        "Mizoram" to listOf(
            "Aizawl", "Champhai", "Hnahthial", "Khawzawl", "Kolasib",
            "Lawngtlai", "Lunglei", "Mamit", "Saitual", "Serchhip", "Siaha"
        ),
        "Nagaland" to listOf(
            "Chumoukedima", "Dimapur", "Kiphire", "Kohima", "Longleng",
            "Mokokchung", "Mon", "Niuland", "Noklak", "Peren",
            "Phek", "Shamator", "Tseminyu", "Tuensang", "Wokha", "Zunheboto"
        ),
        "Odisha" to listOf(
            "Angul", "Balangir", "Balasore", "Bargarh", "Bhadrak",
            "Boudh", "Cuttack", "Deogarh", "Dhenkanal", "Gajapati",
            "Ganjam", "Jagatsinghpur", "Jajpur", "Jharsuguda", "Kalahandi",
            "Kandhamal", "Kendrapara", "Kendujhar", "Khordha", "Koraput",
            "Malkangiri", "Mayurbhanj", "Nabarangpur", "Nayagarh", "Nuapada",
            "Puri", "Rayagada", "Sambalpur", "Subarnapur", "Sundargarh"
        ),
        "Punjab" to listOf(
            "Amritsar", "Barnala", "Bathinda", "Faridkot", "Fatehgarh Sahib",
            "Fazilka", "Ferozepur", "Gurdaspur", "Hoshiarpur", "Jalandhar",
            "Kapurthala", "Ludhiana", "Malerkotla", "Mansa", "Moga",
            "Muktsar", "Pathankot", "Patiala", "Rupnagar", "SAS Nagar",
            "Sangrur", "SBS Nagar", "Sri Muktsar Sahib", "Tarn Taran"
        ),
        "Rajasthan" to listOf(
            "Ajmer", "Alwar", "Anupgarh", "Balotra", "Banswara",
            "Baran", "Barmer", "Beawar", "Bharatpur", "Bhilwara",
            "Bikaner", "Bundi", "Chittorgarh", "Churu", "Dausa",
            "Deeg", "Didwana-Kuchaman", "Dholpur", "Dungarpur", "Ganganagar",
            "Gangapur City", "Hanumangarh", "Jaipur", "Jaipur Rural", "Jaisalmer",
            "Jalore", "Jhalawar", "Jhunjhunu", "Jodhpur", "Jodhpur Rural",
            "Karauli", "Kekri", "Khairthal-Tijara", "Kota", "Kotputli-Behror",
            "Nagaur", "Neem Ka Thana", "Pali", "Phalodi", "Pratapgarh",
            "Rajsamand", "Salumbar", "Sanchore", "Sawai Madhopur", "Shahpura",
            "Sikar", "Sirohi", "Tonk", "Udaipur"
        ),
        "Sikkim" to listOf(
            "Gangtok", "Gyalshing", "Mangan", "Namchi", "Pakyong", "Soreng"
        ),
        "Tamil Nadu" to listOf(
            "Ariyalur", "Chengalpattu", "Chennai", "Coimbatore", "Cuddalore",
            "Dharmapuri", "Dindigul", "Erode", "Kallakurichi", "Kanchipuram",
            "Kanyakumari", "Karur", "Krishnagiri", "Madurai", "Mayiladuthurai",
            "Nagapattinam", "Namakkal", "Nilgiris", "Perambalur", "Pudukkottai",
            "Ramanathapuram", "Ranipet", "Salem", "Sivaganga", "Tenkasi",
            "Thanjavur", "Theni", "Thoothukudi", "Tiruchirappalli", "Tirunelveli",
            "Tirupathur", "Tiruppur", "Tiruvallur", "Tiruvannamalai", "Tiruvarur",
            "Vellore", "Viluppuram", "Virudhunagar"
        ),
        "Telangana" to listOf(
            "Adilabad", "Bhadradri Kothagudem", "Hanamkonda", "Hyderabad", "Jagtial",
            "Jangaon", "Jayashankar Bhupalpally", "Jogulamba Gadwal", "Kamareddy", "Karimnagar",
            "Khammam", "Kumuram Bheem Asifabad", "Mahabubabad", "Mahbubnagar", "Mancherial",
            "Medak", "Medchal-Malkajgiri", "Mulugu", "Nagarkurnool", "Nalgonda",
            "Narayanpet", "Nirmal", "Nizamabad", "Peddapalli", "Rajanna Sircilla",
            "Ranga Reddy", "Sangareddy", "Siddipet", "Suryapet", "Vikarabad",
            "Wanaparthy", "Warangal", "Yadadri Bhuvanagiri"
        ),
        "Tripura" to listOf(
            "Dhalai", "Gomati", "Khowai", "North Tripura", "Sepahijala",
            "South Tripura", "Unakoti", "West Tripura"
        ),
        "Uttar Pradesh" to listOf(
            "Agra", "Aligarh", "Ambedkar Nagar", "Amethi", "Amroha",
            "Auraiya", "Ayodhya", "Azamgarh", "Baghpat", "Bahraich",
            "Ballia", "Balrampur", "Banda", "Barabanki", "Bareilly",
            "Basti", "Bhadohi", "Bijnor", "Budaun", "Bulandshahr",
            "Chandauli", "Chitrakoot", "Deoria", "Etah", "Etawah",
            "Farrukhabad", "Fatehpur", "Firozabad", "Gautam Buddha Nagar", "Ghaziabad",
            "Ghazipur", "Gonda", "Gorakhpur", "Hamirpur", "Hapur",
            "Hardoi", "Hathras", "Jalaun", "Jaunpur", "Jhansi",
            "Kannauj", "Kanpur Dehat", "Kanpur Nagar", "Kasganj", "Kaushambi",
            "Kheri", "Kushinagar", "Lalitpur", "Lucknow", "Maharajganj",
            "Mahoba", "Mainpuri", "Mathura", "Mau", "Meerut",
            "Mirzapur", "Moradabad", "Muzaffarnagar", "Pilibhit", "Pratapgarh",
            "Prayagraj", "Raebareli", "Rampur", "Saharanpur", "Sambhal",
            "Sant Kabir Nagar", "Shahjahanpur", "Shamli", "Shravasti", "Siddharthnagar",
            "Sitapur", "Sonbhadra", "Sultanpur", "Unnao", "Varanasi"
        ),
        "Uttarakhand" to listOf(
            "Almora", "Bageshwar", "Chamoli", "Champawat", "Dehradun",
            "Haridwar", "Nainital", "Pauri Garhwal", "Pithoragarh", "Rudraprayag",
            "Tehri Garhwal", "Udham Singh Nagar", "Uttarkashi"
        ),
        "West Bengal" to listOf(
            "Alipurduar", "Bankura", "Birbhum", "Cooch Behar", "Dakshin Dinajpur",
            "Darjeeling", "Hooghly", "Howrah", "Jalpaiguri", "Jhargram",
            "Kalimpong", "Kolkata", "Malda", "Murshidabad", "Nadia",
            "North 24 Parganas", "Paschim Bardhaman", "Paschim Medinipur", "Purba Bardhaman", "Purba Medinipur",
            "Purulia", "South 24 Parganas", "Uttar Dinajpur"
        ),
        "Andaman and Nicobar Islands" to listOf(
            "Nicobar", "North and Middle Andaman", "South Andaman"
        ),
        "Chandigarh" to listOf(
            "Chandigarh"
        ),
        "Dadra and Nagar Haveli and Daman and Diu" to listOf(
            "Daman", "Diu", "Dadra and Nagar Haveli"
        ),
        "Delhi" to listOf(
            "Central Delhi", "East Delhi", "New Delhi", "North Delhi", "North East Delhi",
            "North West Delhi", "Shahdara", "South Delhi", "South East Delhi", "South West Delhi", "West Delhi"
        ),
        "Jammu and Kashmir" to listOf(
            "Anantnag", "Bandipora", "Baramulla", "Budgam", "Doda",
            "Ganderbal", "Jammu", "Kathua", "Kishtwar", "Kulgam",
            "Kupwara", "Poonch", "Pulwama", "Rajouri", "Ramban",
            "Reasi", "Samba", "Shopian", "Srinagar", "Udhampur"
        ),
        "Ladakh" to listOf(
            "Kargil", "Leh"
        ),
        "Lakshadweep" to listOf(
            "Lakshadweep"
        ),
        "Puducherry" to listOf(
            "Karaikal", "Mahe", "Puducherry", "Yanam"
        )
    )

    fun getDistrictsForState(state: String): List<String> {
        return districtsByState[state] ?: emptyList()
    }
}

/**
 * Standard options and helpers for the multi-step registration flow.
 */
object RegistrationOptions {
    val categories = listOf(
        "General",
        "OBC",
        "SC",
        "ST",
        "EWS"
    )

    val genders = listOf(
        "Male",
        "Female",
        "Other",
        "Prefer not to say"
    )

    val occupations = listOf(
        "Salaried",
        "Self-Employed",
        "Business Owner / Entrepreneur",
        "Farmer / Agriculture",
        "Daily Wage / Laborer",
        "Student",
        "Unemployed / Seeking Work",
        "Homemaker",
        "Retired"
    )

    val incomeRanges = listOf(
        "Below ₹1,00,000",
        "₹1,00,000 – ₹3,00,000",
        "₹3,00,000 – ₹5,00,000",
        "₹5,00,000 – ₹10,00,000",
        "Above ₹10,00,000"
    )

    val educationLevels = listOf(
        "Below 10th",
        "10th Pass (Matriculation)",
        "12th Pass (Higher Secondary)",
        "Diploma / ITI",
        "Graduate / Bachelor's",
        "Post Graduate / Master's",
        "Doctorate / Professional"
    )

    val areaTypes = listOf(
        "Urban",
        "Rural"
    )

    fun mapIncomeRangeToNumber(range: String): Int {
        return when (range) {
            "Below ₹1,00,000" -> 80000
            "₹1,00,000 – ₹3,00,000" -> 200000
            "₹3,00,000 – ₹5,00,000" -> 400000
            "₹5,00,000 – ₹10,00,000" -> 750000
            "Above ₹10,00,000" -> 1200000
            else -> 0
        }
    }
}
