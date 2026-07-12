package com.example.repository

import android.content.Context
import android.util.Log
import com.example.authentication.data.SessionManager
import com.example.models.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

@Singleton
class FirebaseAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager
) : AuthRepository {
    private val TAG = "AuthRepository"

    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    override var isFirebaseEnabled: Boolean = false

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firebaseAuth = FirebaseAuth.getInstance()
                firestore = FirebaseFirestore.getInstance()
                isFirebaseEnabled = true
                Log.d(TAG, "Firebase initialized successfully in Repository.")
            } else {
                Log.w(TAG, "Firebase is not yet initialized. Falling back to local demo mode.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed: ${e.message}. Using local demo mode.", e)
            firebaseAuth = null
            firestore = null
            isFirebaseEnabled = false
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        if (isFirebaseEnabled && firebaseAuth != null) {
            return try {
                val result = firebaseAuth!!.signInWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("UID is null")
                
                var user = User(
                    uid = uid,
                    fullName = email.substringBefore("@"),
                    email = email,
                    role = "Safety Officer"
                )

                if (firestore != null) {
                    try {
                        val doc = firestore!!.collection("users").document(uid).get().await()
                        if (doc.exists()) {
                            user = User(
                                uid = uid,
                                fullName = doc.getString("fullName") ?: doc.getString("name") ?: email.substringBefore("@"),
                                email = doc.getString("email") ?: email,
                                role = doc.getString("role") ?: "Safety Officer",
                                companyCode = doc.getString("companyCode") ?: doc.getString("company") ?: "FAMS Inc",
                                employeeId = doc.getString("employeeId") ?: "",
                                mobile = doc.getString("mobile") ?: "",
                                department = doc.getString("department") ?: "",
                                designation = doc.getString("designation") ?: "",
                                plant = doc.getString("plant") ?: "",
                                status = doc.getString("status") ?: "Active",
                                profilePhoto = doc.getString("profilePhoto") ?: "",
                                createdDate = doc.getLong("createdDate") ?: System.currentTimeMillis(),
                                lastLogin = System.currentTimeMillis(),
                                deviceModel = doc.getString("deviceModel") ?: android.os.Build.MODEL,
                                androidVersion = doc.getString("androidVersion") ?: android.os.Build.VERSION.RELEASE
                            )
                            // Update last login
                            firestore!!.collection("users").document(uid)
                                .update("lastLogin", System.currentTimeMillis())
                        }
                    } catch (fe: Exception) {
                        Log.e(TAG, "Failed to get user details from Firestore, using defaults.", fe)
                    }
                }
                
                sessionManager.saveSession(user)
                _currentUser.value = user
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            // Demo fallback mode
            return if (email == "admin@fams.com" && password == "admin123") {
                val user = User(
                    uid = "admin-uid",
                    fullName = "System Admin",
                    email = email,
                    role = "Administrator",
                    companyCode = "FAMS Global",
                    employeeId = "EMP-001",
                    mobile = "+15551234",
                    department = "Safety Admin",
                    designation = "Lead Auditor",
                    plant = "Main Plant A",
                    name = "System Admin",
                    company = "FAMS Global"
                )
                sessionManager.saveSession(user)
                _currentUser.value = user
                Result.success(user)
            } else if (email == "tech@fams.com" && password == "tech123") {
                val user = User(
                    uid = "tech-uid",
                    fullName = "John Doe",
                    email = email,
                    role = "Fire Technician",
                    companyCode = "FAMS Region A",
                    employeeId = "EMP-002",
                    mobile = "+15555678",
                    department = "Engineering",
                    designation = "Senior Technician",
                    plant = "Chemical Plant B",
                    name = "John Doe",
                    company = "FAMS Region A"
                )
                sessionManager.saveSession(user)
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid credentials for local demo mode. Try (admin@fams.com / admin123) or (tech@fams.com / tech123)"))
            }
        }
    }

    override suspend fun register(name: String, email: String, password: String, role: String): Result<User> {
        val user = User(
            fullName = name,
            email = email,
            role = role,
            name = name
        )
        return registerEnterpriseUser(user, password)
    }

    override suspend fun registerEnterpriseUser(user: User, password: String): Result<User> {
        if (isFirebaseEnabled && firebaseAuth != null) {
            return try {
                // Check unique employeeId or mobile if database exists
                if (isEmployeeIdTaken(user.employeeId)) {
                    return Result.failure(Exception("Employee ID is already registered in the system."))
                }
                if (isMobileNumberTaken(user.mobile)) {
                    return Result.failure(Exception("Mobile Number is already registered in the system."))
                }

                val result = firebaseAuth!!.createUserWithEmailAndPassword(user.email, password).await()
                val uid = result.user?.uid ?: throw Exception("UID is null")
                
                val completeUser = user.copy(
                    uid = uid,
                    createdDate = System.currentTimeMillis(),
                    lastLogin = System.currentTimeMillis(),
                    deviceModel = android.os.Build.MODEL,
                    androidVersion = android.os.Build.VERSION.RELEASE
                )
                
                if (firestore != null) {
                    try {
                        firestore!!.collection("users").document(uid).set(completeUser).await()
                    } catch (fe: Exception) {
                        Log.e(TAG, "Failed to save user in Firestore.", fe)
                    }
                }
                
                sessionManager.saveSession(completeUser)
                _currentUser.value = completeUser
                Result.success(completeUser)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            val completeUser = user.copy(
                uid = "demo-reg-${System.currentTimeMillis()}",
                createdDate = System.currentTimeMillis(),
                lastLogin = System.currentTimeMillis()
            )
            sessionManager.saveSession(completeUser)
            _currentUser.value = completeUser
            return Result.success(completeUser)
        }
    }

    override suspend fun updateProfile(
        mobile: String,
        department: String,
        designation: String,
        plant: String,
        profilePhotoUrl: String
    ): Result<User> {
        val current = _currentUser.value ?: return Result.failure(Exception("No authenticated user session found."))
        
        // Validation check for duplicate mobile if changed
        if (mobile != current.mobile && isMobileNumberTaken(mobile)) {
            return Result.failure(Exception("Mobile Number is already registered by another user."))
        }

        val updated = current.copy(
            mobile = mobile,
            department = department,
            designation = designation,
            plant = plant,
            profilePhoto = profilePhotoUrl
        )

        if (isFirebaseEnabled && firestore != null) {
            try {
                firestore!!.collection("users").document(current.uid).set(updated).await()
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }

        sessionManager.saveSession(updated)
        _currentUser.value = updated
        return Result.success(updated)
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        if (isFirebaseEnabled && firebaseAuth != null) {
            return try {
                val fbUser = firebaseAuth!!.currentUser ?: throw Exception("No authenticated user")
                val email = fbUser.email ?: throw Exception("User email is null")
                
                // Reauthenticate first to secure password change
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, oldPassword)
                fbUser.reauthenticate(credential).await()
                
                // Update password
                fbUser.updatePassword(newPassword).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            return Result.success(Unit)
        }
    }

    override suspend fun isEmployeeIdTaken(employeeId: String): Boolean {
        if (employeeId.isEmpty()) return false
        if (isFirebaseEnabled && firestore != null) {
            return try {
                val snapshot = firestore!!.collection("users")
                    .whereEqualTo("employeeId", employeeId)
                    .get()
                    .await()
                !snapshot.isEmpty
            } catch (e: Exception) {
                false
            }
        }
        return false
    }

    override suspend fun isMobileNumberTaken(mobile: String): Boolean {
        if (mobile.isEmpty()) return false
        if (isFirebaseEnabled && firestore != null) {
            return try {
                val snapshot = firestore!!.collection("users")
                    .whereEqualTo("mobile", mobile)
                    .get()
                    .await()
                !snapshot.isEmpty
            } catch (e: Exception) {
                false
            }
        }
        return false
    }

    override suspend fun logout(): Result<Unit> {
        if (isFirebaseEnabled && firebaseAuth != null) {
            firebaseAuth!!.signOut()
        }
        sessionManager.clearSession()
        _currentUser.value = null
        return Result.success(Unit)
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        if (isFirebaseEnabled && firebaseAuth != null) {
            return try {
                firebaseAuth!!.sendPasswordResetEmail(email).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            return Result.success(Unit)
        }
    }

    override suspend fun checkAutoLogin(): Result<User?> {
        // First, check the local DataStore session cache
        val localUser = sessionManager.getSession()
        if (localUser != null) {
            _currentUser.value = localUser
            Log.d(TAG, "Restored session locally from DataStore: ${localUser.email}")
            return Result.success(localUser)
        }

        // Fallback to checking Firebase SDK
        if (isFirebaseEnabled && firebaseAuth != null) {
            val fbUser = firebaseAuth!!.currentUser
            if (fbUser != null) {
                val uid = fbUser.uid
                val email = fbUser.email ?: ""
                var user = User(uid = uid, fullName = email.substringBefore("@"), email = email)
                if (firestore != null) {
                    try {
                        val doc = firestore!!.collection("users").document(uid).get().await()
                        if (doc.exists()) {
                            user = User(
                                uid = uid,
                                fullName = doc.getString("fullName") ?: doc.getString("name") ?: email.substringBefore("@"),
                                email = doc.getString("email") ?: email,
                                role = doc.getString("role") ?: "Safety Officer",
                                companyCode = doc.getString("companyCode") ?: doc.getString("company") ?: "FAMS Inc",
                                employeeId = doc.getString("employeeId") ?: "",
                                mobile = doc.getString("mobile") ?: "",
                                department = doc.getString("department") ?: "",
                                designation = doc.getString("designation") ?: "",
                                plant = doc.getString("plant") ?: "",
                                status = doc.getString("status") ?: "Active",
                                profilePhoto = doc.getString("profilePhoto") ?: "",
                                createdDate = doc.getLong("createdDate") ?: System.currentTimeMillis(),
                                lastLogin = System.currentTimeMillis(),
                                deviceModel = doc.getString("deviceModel") ?: android.os.Build.MODEL,
                                androidVersion = doc.getString("androidVersion") ?: android.os.Build.VERSION.RELEASE
                            )
                        }
                    } catch (fe: Exception) {
                        Log.e(TAG, "Failed to restore user from Firestore.", fe)
                    }
                }
                sessionManager.saveSession(user)
                _currentUser.value = user
                return Result.success(user)
            }
        }
        return Result.success(null)
    }
}
