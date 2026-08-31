interface BrandMarkProps {
  className?: string;
}

export default function BrandMark({
  className = "",
}: BrandMarkProps) {
  return (
    <img
      src="/public/images/brand/rebby-store-mark.png"
      alt="RebbyStore"
      className={`object-contain ${className}`}
    />
  );
}