package com.example.localgrubshop.utils

object OrderStatus {
    const val PLACED = "Placed"
    const val CONFIRMED = "Confirmed"
    const val PREPARING = "Preparing"
    const val OUT_FOR_DELIVERY = "Out for Delivery"
    const val DELIVERED = "Delivered"
    const val CANCELLED = "Cancelled"
}

object DishFields {
    const val COLLECTION = "dishes"
    const val IN_STOCK = "available"
}

object OrderFields {
    const val COLLECTION = "orders"
    const val STATUS = "status"
}

object ShopOwnerFields {
    const val COLLECTION = "admins"
    const val TOKEN_COLLECTION_NAME = "tokens"
    const val TOKEN = "fcmToken"
}

object UserFields {
    const val COLLECTION = "users"
    const val TOKEN_COLLECTION = "tokens"
}

object OfferType {
    const val PERCENTAGE = "Percentage"
    const val FIXED = "Fixed"
}

object OfferStatus {
    const val ACTIVE = "Active"
    const val INACTIVE = "Inactive"
    const val EXPIRED = "Expired"
}

object OfferConstant {
    const val COLLECTION_NAME = "offers"
    const val OFFER_STATUS = "offerStatus"
}