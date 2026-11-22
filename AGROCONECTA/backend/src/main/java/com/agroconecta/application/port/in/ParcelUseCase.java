package com.agroconecta.application.port.in;

import com.agroconecta.application.command.ParcelCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.Parcel;

public interface ParcelUseCase {

    PageResult<Parcel> list(Long ownerId, PageQuery pageQuery);

    Parcel get(Long ownerId, Long parcelId);

    Parcel create(Long ownerId, ParcelCommand command);

    Parcel update(Long ownerId, Long parcelId, ParcelCommand command);

    void delete(Long ownerId, Long parcelId);
}
