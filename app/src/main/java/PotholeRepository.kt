object PotholeRepository {

    val potholeList = mutableListOf<Pothole>()

    fun addPothole(pothole: Pothole) {
        potholeList.add(pothole)
    }

    fun getCount(): Int {
        return potholeList.size
    }
}