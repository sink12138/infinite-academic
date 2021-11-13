<template>
  <div>
    <h1>Search</h1>
    <BaseGoBack class="back"></BaseGoBack>
    <BaseSearchBar class="search"></BaseSearchBar>
    <BaseFilter class="filter"></BaseFilter>
  </div>
</template>

<script>
import BaseFilter from "../components/BaseFilter.vue";
import BaseGoBack from "../components/BaseGoBack.vue";
import BaseSearchBar from "../components/BaseSearchBar.vue";

export default {
  components: {
    BaseGoBack,
    BaseFilter,
    BaseSearchBar,
  },
};
</script>

<style>
.filter {
  position: fixed;
  left: 0;
}
.back {
  position: fixed;
  left: 20px;
}
.search{
  margin-bottom: 30px;
}
</style>