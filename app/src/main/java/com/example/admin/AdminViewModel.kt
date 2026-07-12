package com.example.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.Activity
import com.example.models.Asset
import com.example.models.User
import com.example.repository.AssetRepository
import com.example.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardState(
    val isLoading: Boolean = false,
    val totalUsers: Int = 142,
    val onlineUsers: Int = 12,
    val totalPlants: Int = 6,
    val totalAssets: Int = 1240,
    val inspectionCompliance: Float = 0.965f,
    val maintenanceCompliance: Float = 0.921f,
    val inventoryStatus: Float = 0.984f,
    val pendingApprovals: Int = 7,
    val criticalAlertsCount: Int = 3,
    val systemHealth: Float = 0.999f,
    val userActivityData: List<Int> = listOf(45, 62, 58, 74, 91, 85, 110),
    val inspectionTrendData: List<Int> = listOf(120, 145, 138, 162, 175, 190, 215),
    val maintenanceTrendData: List<Int> = listOf(34, 45, 41, 52, 48, 60, 55),
    val assetDistribution: Map<String, Float> = mapOf(
        "Fire Extinguisher" to 420f,
        "Smoke Detector" to 510f,
        "Fire Hydrant" to 110f,
        "Sprinkler" to 120f,
        "Alarm Panel" to 30f,
        "Hose Reel" to 50f
    )
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val assetRepository: AssetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminDashboardState())
    val state: StateFlow<AdminDashboardState> = _state.asStateFlow()

    // Logs & Active lists for user management
    private val _usersList = MutableStateFlow<List<User>>(emptyList())
    val usersList: StateFlow<List<User>> = _usersList.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    private val _plantsList = MutableStateFlow<List<PlantConfig>>(emptyList())
    val plantsList: StateFlow<List<PlantConfig>> = _plantsList.asStateFlow()

    private val _customRolesList = MutableStateFlow<List<CustomRole>>(emptyList())
    val customRolesList: StateFlow<List<CustomRole>> = _customRolesList.asStateFlow()

    init {
        loadAdminData()
    }

    fun refreshData() {
        loadAdminData()
    }

    private fun loadAdminData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // Pre-populate mock/initial users representing central corporate directory
                val users = listOf(
                    User(uid = "admin1", fullName = "Deenadhayalan", email = "deenadhayalan3003@gmail.com", role = "Super Administrator", department = "Corporate Safety", plant = "Plant 1", status = "Active"),
                    User(uid = "user2", fullName = "Arun Kumar", email = "arun.kumar@fams.com", role = "Plant Head", department = "Operations", plant = "Plant 1", status = "Active"),
                    User(uid = "user3", fullName = "Sarah Jenkins", email = "sarah.j@fams.com", role = "Safety Manager", department = "Safety Inspection", plant = "Plant 2", status = "Active"),
                    User(uid = "user4", fullName = "Michael Chang", email = "michael.c@fams.com", role = "Fire Technician", department = "Maintenance", plant = "Plant 3", status = "Active"),
                    User(uid = "user5", fullName = "Elena Rostova", email = "elena.r@fams.com", role = "Auditor", department = "Compliance", plant = "Plant 1", status = "Active"),
                    User(uid = "user6", fullName = "John Doe", email = "john.doe@fams.com", role = "Viewer", department = "External Audit", plant = "Plant 4", status = "Inactive")
                )
                _usersList.value = users

                // Pre-populate industrial plant complexes
                val plants = listOf(
                    PlantConfig(id = "plant_1", name = "Plant 1", location = "Chennai Central", manager = "Arun Kumar", zonesCount = 8, activeStaffCount = 34, safetyScore = 98.4f, status = "Operational"),
                    PlantConfig(id = "plant_2", name = "Plant 2", location = "Bangalore North", manager = "Sarah Jenkins", zonesCount = 6, activeStaffCount = 28, safetyScore = 95.1f, status = "Operational"),
                    PlantConfig(id = "plant_3", name = "Plant 3", location = "Hyderabad Phase 2", manager = "Michael Chang", zonesCount = 12, activeStaffCount = 45, safetyScore = 91.2f, status = "Maintenance"),
                    PlantConfig(id = "plant_4", name = "Plant 4", location = "Mumbai Hub", manager = "Elena Rostova", zonesCount = 4, activeStaffCount = 15, safetyScore = 88.9f, status = "Suspended")
                )
                _plantsList.value = plants

                // Pre-populate standard system and custom roles
                val customRoles = listOf(
                    CustomRole(id = "sys_admin", name = "Super Administrator", description = "Complete access to all operations, compliance audits, and configuration consoles.", permissions = setOf("asset:read", "asset:write", "inspection:log", "inspection:approve", "user:manage"), isSystemRole = true),
                    CustomRole(id = "sys_mgr", name = "Safety Manager", description = "Inspect assets, review safety standards, and sign-off monthly compliance checklists.", permissions = setOf("asset:read", "asset:write", "inspection:log", "inspection:approve"), isSystemRole = true),
                    CustomRole(id = "sys_tech", name = "Fire Technician", description = "Perform routine asset testing, upload defects, and scan dynamic QR codes.", permissions = setOf("asset:read", "inspection:log"), isSystemRole = true),
                    CustomRole(id = "role_hazmat", name = "Hazmat Inspector", description = "Specialized inspector for hazardous chemical asset storage safety.", permissions = setOf("asset:read", "inspection:log")),
                    CustomRole(id = "role_contract", name = "Contract Worker", description = "External worker with restricted view-only asset tracking capabilities.", permissions = setOf("asset:read"))
                )
                _customRolesList.value = customRoles

                // Pre-populate professional audit log audit entries
                val logs = listOf(
                    AuditLogEntry(action = "USER_LOGIN", user = "deenadhayalan3003@gmail.com", role = "Super Administrator", module = "Authentication", details = "Successful login from Android Device (SDK 33)"),
                    AuditLogEntry(action = "ASSET_CREATE", user = "sarah.j@fams.com", role = "Safety Manager", module = "Asset Management", details = "Created Fire Extinguisher FE-109 in Zone B"),
                    AuditLogEntry(action = "ROLE_UPDATE", user = "deenadhayalan3003@gmail.com", role = "Super Administrator", module = "User Administration", details = "Role of user4 updated from Fire Technician to Safety Officer"),
                    AuditLogEntry(action = "INSPECTION_APPROVE", user = "arun.kumar@fams.com", role = "Plant Head", module = "Workflow Approvals", details = "Approved monthly inspection log for Hydrant HYD-02"),
                    AuditLogEntry(action = "SETTING_EDIT", user = "deenadhayalan3003@gmail.com", role = "Super Administrator", module = "System Config", details = "Changed QR scanning verification requirements fallback to strict mode")
                )
                _auditLogs.value = logs

                // Try to load actual counts if assetRepository is working
                assetRepository.assets.collect { assetsList ->
                    if (assetsList.isNotEmpty()) {
                        val activeCount = assetsList.size
                        val compliance = assetsList.count { it.status.equals("Normal", ignoreCase = true) }.toFloat() / activeCount.coerceAtLeast(1)
                        _state.value = _state.value.copy(
                            totalAssets = activeCount,
                            inspectionCompliance = compliance
                        )
                    }
                }
            } catch (e: Exception) {
                // Fallback gracefully
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    // Admin Operations
    fun updateUserRole(userId: String, newRole: String) {
        _usersList.value = _usersList.value.map {
            if (it.uid == userId) it.copy(role = newRole) else it
        }
        logAdminAction("ROLE_UPDATE", "Updated role for user ID $userId to $newRole")
    }

    fun updateUserStatus(userId: String, newStatus: String) {
        _usersList.value = _usersList.value.map {
            if (it.uid == userId) it.copy(status = newStatus) else it
        }
        logAdminAction("STATUS_UPDATE", "Changed status for user ID $userId to $newStatus")
    }

    fun createAdminUser(fullName: String, email: String, role: String, department: String, plant: String) {
        createAdvancedUser(
            fullName = fullName,
            email = email,
            role = role,
            department = department,
            plant = plant,
            mobile = "+1 555-0199",
            employeeId = "EMP-" + (1000..9999).random(),
            companyCode = "FAMS-GLOBAL",
            expiryDuration = "Never",
            customPermissions = setOf("asset:read", "inspection:log")
        )
    }

    fun createAdvancedUser(
        fullName: String,
        email: String,
        role: String,
        department: String,
        plant: String,
        mobile: String,
        employeeId: String,
        companyCode: String,
        expiryDuration: String,
        customPermissions: Set<String>
    ) {
        val newUser = User(
            uid = "user_" + System.currentTimeMillis(),
            fullName = fullName,
            email = email,
            role = role,
            department = department,
            plant = plant,
            mobile = mobile,
            employeeId = employeeId,
            companyCode = companyCode,
            status = "Active",
            designation = "$role ($department)"
        )
        _usersList.value = _usersList.value + newUser
        logAdminAction(
            "USER_PROVISIONED",
            "Manually provisioned advanced account: $email, Role: $role, Emp ID: $employeeId, Expiry: $expiryDuration, Permissions: ${customPermissions.joinToString(", ")}"
        )
    }

    // Plant Configurations
    fun createPlant(name: String, location: String, manager: String, zonesCount: Int) {
        val newPlant = PlantConfig(
            id = "plant_" + System.currentTimeMillis(),
            name = name,
            location = location,
            manager = manager,
            zonesCount = zonesCount,
            activeStaffCount = (5..30).random(),
            safetyScore = 100.0f,
            status = "Operational"
        )
        _plantsList.value = _plantsList.value + newPlant
        _state.value = _state.value.copy(totalPlants = _plantsList.value.size)
        logAdminAction("PLANT_CREATE", "Configured new Industrial Plant Complex: $name in $location administered by $manager")
    }

    fun updatePlant(id: String, name: String, location: String, manager: String, zonesCount: Int, status: String, safetyScore: Float) {
        _plantsList.value = _plantsList.value.map {
            if (it.id == id) {
                it.copy(
                    name = name,
                    location = location,
                    manager = manager,
                    zonesCount = zonesCount,
                    status = status,
                    safetyScore = safetyScore
                )
            } else it
        }
        logAdminAction("PLANT_UPDATE", "Updated details for Plant Complex $name ($status)")
    }

    fun deletePlant(id: String) {
        val plant = _plantsList.value.find { it.id == id }
        _plantsList.value = _plantsList.value.filter { it.id != id }
        _state.value = _state.value.copy(totalPlants = _plantsList.value.size)
        logAdminAction("PLANT_DELETE", "Decommissioned Plant Complex: ${plant?.name ?: id}")
    }

    // Custom Roles
    fun createCustomRole(name: String, description: String, permissions: Set<String>) {
        val id = "role_" + name.lowercase().replace(" ", "_").replace("[^a-z0-9_]".toRegex(), "")
        val newRole = CustomRole(
            id = id,
            name = name,
            description = description,
            permissions = permissions
        )
        _customRolesList.value = _customRolesList.value + newRole
        logAdminAction("ROLE_CREATE", "Designed new Enterprise Custom Role: $name with ${permissions.size} permissions")
    }

    fun deleteCustomRole(id: String) {
        val role = _customRolesList.value.find { it.id == id }
        if (role?.isSystemRole != true) {
            _customRolesList.value = _customRolesList.value.filter { it.id != id }
            logAdminAction("ROLE_DELETE", "Retired custom corporate role: ${role?.name ?: id}")
        }
    }

    private fun logAdminAction(action: String, details: String) {
        val newEntry = AuditLogEntry(
            timestamp = System.currentTimeMillis(),
            action = action,
            user = "deenadhayalan3003@gmail.com",
            role = "Super Administrator",
            module = "Enterprise Administration",
            details = details
        )
        _auditLogs.value = listOf(newEntry) + _auditLogs.value
    }
}

data class AuditLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val user: String,
    val role: String,
    val module: String,
    val details: String,
    val plant: String = "Plant 1"
)

data class PlantConfig(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val manager: String = "",
    val zonesCount: Int = 1,
    val activeStaffCount: Int = 0,
    val safetyScore: Float = 98.5f,
    val status: String = "Operational"
)

data class CustomRole(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val permissions: Set<String> = emptySet(),
    val isSystemRole: Boolean = false
)
