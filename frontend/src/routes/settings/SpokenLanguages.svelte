<script lang="ts">
  import Label from "@/components/ui/label/label.svelte";
  import SearchableCombobox from "../../components/SearchableCombobox.svelte";
  import { Languages } from "../../data/Languages";
  import { backendUrl } from "../../util/GuiConfig";
  import { Toaster } from "@/components/ui/sonner";
  import { toast } from "svelte-sonner";
  import Button from "@/components/ui/button/button.svelte";

  let {
    spokenLanguages = [],
  }: {
    spokenLanguages: Array<string>;
  } = $props();

  $effect(() => {
    if (spokenLanguages) {
      updateSpokenLanguages();
    }
  });

  async function updateSpokenLanguages() {
    let distinctLanguages = new Set(
      spokenLanguages.filter((it) => it.length !== 0),
    );

    if (distinctLanguages.size === 0) return;

    const response = await fetch(`${backendUrl}/gui/settings/spokenLanguages`, {
      method: "POST",
      headers: {
        "Content-type": "application/json; charset=UTF-8",
      },
      body: JSON.stringify({
        languages: Array.from(distinctLanguages.values()),
      }),
      credentials: "include",
    });

    if (!response.ok) {
      toast.error("Failed to update spoken language");
    }
  }

  function remove(index: number) {
    spokenLanguages.splice(index, 1);
  }
</script>

<div>
  <Label class="text-lg">Native or near-native Languages:</Label>
  <div class="flex flex-col pl-4 gap-2">
    {#each { length: spokenLanguages.length + 1 }, index}
      <div class="flex flex-row m-0 gap-2">
        <Label class="text-base col-span-1">{index + 1}.</Label>
        <div class="col-span-2 max-w">
          <SearchableCombobox
            options={Object.values(Languages)}
            bind:selected={spokenLanguages[index]}
          />
        </div>
        <Button
          variant="destructive"
          class="col-span-1"
          onclick={() => remove(index)}>remove</Button
        >
      </div>
    {/each}
  </div>
</div>

<Toaster />
