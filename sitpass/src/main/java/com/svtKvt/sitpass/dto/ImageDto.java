package com.svtKvt.sitpass.dto;

import com.svtKvt.sitpass.model.Facility;
import com.svtKvt.sitpass.model.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageDto {
    private long id;
    private String path;
    private Long facilityId;

    public static ImageDto convertToDto(Image image) {
        return ImageDto.builder()
                .id(image.getId())
                .path(image.getPath())
                .facilityId(image.getFacility().getId())
                .build();
    }

    public Image convertToModel() {
        Image image = new Image();
        image.setId(this.id);
        image.setPath(this.path);
        // facility should be set separately in the service layer to avoid cyclic dependency
        return image;
    }
}
