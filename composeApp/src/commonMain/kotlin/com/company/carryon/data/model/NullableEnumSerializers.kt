package com.company.carryon.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class NullableEnumSerializer<T : Enum<T>>(
    serialName: String,
    private val values: List<T>
) : KSerializer<T?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): T? {
        val name = decoder.decodeString().trim()
        if (name.isEmpty()) return null
        return values.firstOrNull { it.name == name }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: T?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value.name)
        }
    }
}

object NullableDriverNationalitySerializer : NullableEnumSerializer<DriverNationality>(
    "DriverNationality",
    DriverNationality.entries
)

object NullableLicenseClassSerializer : NullableEnumSerializer<LicenseClass>(
    "LicenseClass",
    LicenseClass.entries
)

object NullableMalaysianStateSerializer : NullableEnumSerializer<MalaysianState>(
    "MalaysianState",
    MalaysianState.entries
)

object NullableVehicleTypeSerializer : NullableEnumSerializer<VehicleType>(
    "VehicleType",
    VehicleType.entries
)

object NullableVehicleOwnershipSerializer : NullableEnumSerializer<VehicleOwnership>(
    "VehicleOwnership",
    VehicleOwnership.entries
)

object NullableInsuranceCoverageTypeSerializer : NullableEnumSerializer<InsuranceCoverageType>(
    "InsuranceCoverageType",
    InsuranceCoverageType.entries
)
