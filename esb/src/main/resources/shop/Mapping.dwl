%dw 2.0
ns soap http://schemas.xmlsoap.org/soap/envelope/
ns shop http://itmo.org/soa/shop

fun body(message) = message.soap#Envelope.soap#Body

fun number(value) = if (value == null) null else value as Number

fun vehicle(v) = {
    id: v.id as Number,
    name: v.name,
    coordinates: {x: v.coordinates.x as Number, y: v.coordinates.y as Number},
    creationDate: v.creationDate,
    enginePower: v.enginePower as Number,
    numberOfWheels: number(v.numberOfWheels),
    mileage: number(v.mileage),
    'type': v.'type',
    fuelType: v.fuelType
}

fun vehicles(message) = (body(message).shop#searchByTypeResponse.*vehicle default []) map vehicle($)

fun fixedVehicle(message) = vehicle(body(message).shop#fixDistanceResponse.vehicle)

fun problem(message, instance) = do {
    var fault = body(message).soap#Fault.detail.shop#ShopFault
    var errors = fault.*error default []
    ---
    {
        'type': 'about:blank',
        title: fault.title default 'Internal Server Error',
        status: (fault.status default '500') as Number,
        detail: fault.detail default 'Internal service error.',
        instance: instance
    } ++ (if (isEmpty(errors)) {} else {errors: errors map {field: $.field, message: $.message}})
}

fun internalError(instance) = {
    'type': 'about:blank',
    title: 'Internal Server Error',
    status: 500,
    detail: 'Internal service error.',
    instance: instance
}
