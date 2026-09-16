package com.yugabyte.app.yugastore.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.RepresentationModel;

class ImageInfoTest {

  @Test
  void extendsRepresentationModel() {
    assertThat(new ImageInfo()).isInstanceOf(RepresentationModel.class);
  }

  @Test
  void constructorInitializesOnlyAlsoBoughtAndAlsoViewed() {
    // Quirk: boughtTogether is left null by the constructor.
    ImageInfo info = new ImageInfo();

    assertThat(info.getAlsoBought()).isEmpty();
    assertThat(info.getAlsoViewed()).isEmpty();
    assertThat(info.getBoughtTogether()).isNull();
  }

  @Test
  void settersWork() {
    ImageInfo info = new ImageInfo();
    List<String> bought = Arrays.asList("B002");
    List<String> viewed = Arrays.asList("B003");
    List<String> together = Arrays.asList("B004");

    info.setAlsoBought(bought);
    info.setAlsoViewed(viewed);
    info.setBoughtTogether(together);

    assertThat(info.getAlsoBought()).isSameAs(bought);
    assertThat(info.getAlsoViewed()).isSameAs(viewed);
    assertThat(info.getBoughtTogether()).isSameAs(together);
  }
}
