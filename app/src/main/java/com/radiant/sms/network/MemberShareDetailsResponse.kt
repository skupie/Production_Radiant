package com.radiant.sms.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

private fun firstNotBlank(vararg values: String?): String? {
    return values.firstOrNull { !it.isNullOrBlank() }
}

@JsonClass(generateAdapter = true)
data class MemberShareDetailsResponse(
    // Common nested variants
    @Json(name = "member") val member: MemberInfo? = null,

    @Json(name = "share") val share: ShareInfo? = null,
    @Json(name = "share_info") val shareInfo: ShareInfo? = null,
    @Json(name = "share_details") val shareDetails: ShareInfo? = null,

    @Json(name = "nominee") val nominee: NomineeInfo? = null,
    @Json(name = "nominee_info") val nomineeInfo: NomineeInfo? = null,
    @Json(name = "nominee_details") val nomineeDetails: NomineeInfo? = null,

    // Flat fallback variants
    @Json(name = "share_no") val shareNoFlat: String? = null,
    @Json(name = "share_amount") val shareAmountFlat: String? = null,
    @Json(name = "total_deposit") val totalDepositFlat: String? = null,
    @Json(name = "created_at") val createdAtFlat: String? = null,

    @Json(name = "nominee_name") val nomineeNameFlat: String? = null,
    @Json(name = "nominee_phone") val nomineePhoneFlat: String? = null,
    @Json(name = "nominee_relation") val nomineeRelationFlat: String? = null,
    @Json(name = "nominee_nid") val nomineeNidFlat: String? = null,
    @Json(name = "nominee_address") val nomineeAddressFlat: String? = null,
    @Json(name = "nominee_photo") val nomineePhotoFlat: String? = null,
    @Json(name = "nominee_photo_url") val nomineePhotoUrlFlat: String? = null
) {

    val resolvedShare: ShareInfo?
        get() = share ?: shareInfo ?: shareDetails ?: run {
            if (
                shareNoFlat == null &&
                shareAmountFlat == null &&
                totalDepositFlat == null &&
                createdAtFlat == null
            ) {
                null
            } else {
                ShareInfo(
                    shareNoSnake = shareNoFlat,
                    shareAmountSnake = shareAmountFlat,
                    totalDepositSnake = totalDepositFlat,
                    createdAtSnake = createdAtFlat
                )
            }
        }

    /**
     * FIX:
     * Some APIs return nominee name/nid inside nested nominee object,
     * but nominee photo as flat nominee_photo / nominee_photo_url.
     * So merge both instead of choosing only one source.
     */
    val resolvedNominee: NomineeInfo?
        get() {
            val base = nominee ?: nomineeInfo ?: nomineeDetails

            val merged = NomineeInfo(
                name = firstNotBlank(base?.name, nomineeNameFlat),
                fullName = base?.fullName,
                nomineeName = base?.nomineeName,

                phone = firstNotBlank(base?.phone, nomineePhoneFlat),
                mobileNumber = base?.mobileNumber,

                relation = firstNotBlank(base?.relation, nomineeRelationFlat),

                nid = firstNotBlank(base?.nid, nomineeNidFlat),
                nomineeNid = base?.nomineeNid,
                nationalId = base?.nationalId,

                address = firstNotBlank(base?.address, nomineeAddressFlat),

                photo = firstNotBlank(base?.photo, nomineePhotoFlat),
                image = base?.image,
                avatar = base?.avatar,
                profilePhoto = base?.profilePhoto,

                profilePhotoUrl = firstNotBlank(base?.profilePhotoUrl, nomineePhotoUrlFlat),
                nomineePhoto = firstNotBlank(base?.nomineePhoto, nomineePhotoFlat),
                nomineePhotoUrl = firstNotBlank(base?.nomineePhotoUrl, nomineePhotoUrlFlat),

                imageUrl = base?.imageUrl,
                photoUrl = base?.photoUrl
            )

            return if (
                firstNotBlank(
                    merged.displayName,
                    merged.displayPhone,
                    merged.relation,
                    merged.displayNid,
                    merged.address,
                    merged.displayPhotoUrl
                ).isNullOrBlank()
            ) {
                null
            } else {
                merged
            }
        }
}

@JsonClass(generateAdapter = true)
data class MemberInfo(
    @Json(name = "name") val name: String? = null,
    @Json(name = "full_name") val fullName: String? = null,

    @Json(name = "email") val email: String? = null,

    @Json(name = "phone") val phone: String? = null,
    @Json(name = "mobile_number") val mobileNumber: String? = null,

    @Json(name = "member_id") val memberIdSnake: String? = null,
    @Json(name = "id") val id: String? = null,

    @Json(name = "nid") val nid: String? = null,
    @Json(name = "national_id") val nationalId: String? = null,
    @Json(name = "member_nid") val memberNid: String? = null,

    @Json(name = "photo") val photo: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "profile_photo") val profilePhoto: String? = null,
    @Json(name = "profile_photo_url") val profilePhotoUrl: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "photo_url") val photoUrl: String? = null
) {
    val displayName: String?
        get() = firstNotBlank(name, fullName)

    val displayPhone: String?
        get() = firstNotBlank(phone, mobileNumber)

    val displayMemberId: String?
        get() = firstNotBlank(memberIdSnake, id)

    val displayNid: String?
        get() = firstNotBlank(nid, nationalId, memberNid)

    val displayPhotoUrl: String?
        get() = NetworkModule.absoluteUrl(
            firstNotBlank(
                profilePhotoUrl,
                imageUrl,
                photoUrl,
                profilePhoto,
                avatar,
                image,
                photo
            )
        )
}

@JsonClass(generateAdapter = true)
data class ShareInfo(
    @Json(name = "share_no") val shareNoSnake: String? = null,
    @Json(name = "shareNo") val shareNoCamel: String? = null,

    @Json(name = "share_amount") val shareAmountSnake: String? = null,
    @Json(name = "shareAmount") val shareAmountCamel: String? = null,

    @Json(name = "total_deposit") val totalDepositSnake: String? = null,
    @Json(name = "totalDeposit") val totalDepositCamel: String? = null,

    @Json(name = "created_at") val createdAtSnake: String? = null,
    @Json(name = "createdAt") val createdAtCamel: String? = null
) {
    val displayShareNo: String?
        get() = firstNotBlank(shareNoSnake, shareNoCamel)

    val displayShareAmount: String?
        get() = firstNotBlank(shareAmountSnake, shareAmountCamel)

    val displayTotalDeposit: String?
        get() = firstNotBlank(totalDepositSnake, totalDepositCamel)

    val displayCreatedAt: String?
        get() = firstNotBlank(createdAtSnake, createdAtCamel)
}

@JsonClass(generateAdapter = true)
data class NomineeInfo(
    @Json(name = "name") val name: String? = null,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "nominee_name") val nomineeName: String? = null,

    @Json(name = "phone") val phone: String? = null,
    @Json(name = "mobile_number") val mobileNumber: String? = null,

    @Json(name = "relation") val relation: String? = null,

    @Json(name = "nid") val nid: String? = null,
    @Json(name = "nominee_nid") val nomineeNid: String? = null,
    @Json(name = "national_id") val nationalId: String? = null,

    @Json(name = "address") val address: String? = null,

    @Json(name = "photo") val photo: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "profile_photo") val profilePhoto: String? = null,
    @Json(name = "profile_photo_url") val profilePhotoUrl: String? = null,

    @Json(name = "nominee_photo") val nomineePhoto: String? = null,
    @Json(name = "nominee_photo_url") val nomineePhotoUrl: String? = null,

    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "photo_url") val photoUrl: String? = null
) {
    val displayName: String?
        get() = firstNotBlank(name, fullName, nomineeName)

    val displayPhone: String?
        get() = firstNotBlank(phone, mobileNumber)

    val displayNid: String?
        get() = firstNotBlank(nid, nomineeNid, nationalId)

    val displayPhotoUrl: String?
        get() = NetworkModule.absoluteUrl(
            firstNotBlank(
                profilePhotoUrl,
                nomineePhotoUrl,
                imageUrl,
                photoUrl,
                profilePhoto,
                nomineePhoto,
                avatar,
                image,
                photo
            )
        )
}
